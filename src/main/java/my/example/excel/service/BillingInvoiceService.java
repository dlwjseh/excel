package my.example.excel.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.example.excel.ExcelBorder;
import my.example.excel.ExcelCell;
import my.example.excel.ExcelConfig;
import my.example.excel.ExcelStyle;
import my.example.excel.ExcelWriter;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingInvoiceService {
	private final DataService dataService;
	private final NamedParameterJdbcTemplate jdbcTemplate;

	// 엑셀 스타일
	private final String HEADER = "Header";
	private final String COMMON = "Text";
	private final String AMOUNT = "Amount";
	private final String SUM_COMMON = "SumCommon";
	private final String SUM_AMOUNT = "SumAmount";
	private final String SUM_WON_AMOUNT = "SumWonAmount";
	private final String SUM_HIGHLIGHT_WON_AMOUNT = "SumWonHighAmount";
	private final String USAGE = "Usage";
	private final String USAGE_AMOUNT = "UsageAmount";
	private final String SUPPORT = "Support";
	private final String SUPPORT_AMOUNT = "SupportAmount";
	private final String PERCENT = "Percent";

	@Transactional(readOnly = true)
	public ExcelWriter getWriter(Long partnerId, Long businessId, String ym, String invoiceLevel, String invoiceItem) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("partnerId", partnerId);
		params.addValue("businessId", businessId);
		params.addValue("invoiceLevel", invoiceLevel);
		params.addValue("invoiceItem", invoiceItem);
		params.addValue("ym", ym);

		// Account name 조회
		Map<String, String> accountMap = dataService.selectList("BILLING_INVOICE_DETAIL_ACCOUNT_NAME", params,
						(rs, idx) -> Map.of(rs.getString("ACCOUNTID"),
								rs.getString("PROJECTNAME")))
				.stream()
				.flatMap(map -> map.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// 상세 이용 내역 조회 쿼리
		Map<String, Object> queryResult = dataService.selectOne("BILLING_INVOICE_DETAIL_QUERY", params);
		String invoiceqry = (String) queryResult.get("INVOICEQRY");
		//조회 존재 체크
		if (invoiceqry == null) {
			throw new RuntimeException("Not found invoice query");
		}

		invoiceqry = invoiceqry.replace(";", "");
		log.debug("invoiceqry: {}", invoiceqry);

		SqlRowSet rs = jdbcTemplate.queryForRowSet(invoiceqry, Map.of());
		SqlRowSetMetaData metaData = rs.getMetaData();

		// accountMap과 rs로 Excel 그리기 시작
		ExcelConfig config = getExcelConfig("테스트", metaData.getColumnNames());
		return ExcelWriter.builder(config)
				// Header
				.addRows(accountMap, metaData, (accMap, meta) -> {
					// 1번쨰 헤더
					List<ExcelCell> h1 = new ArrayList<>();
					h1.add(new ExcelCell("Group", 1, 2, HEADER));

					// 프로젝트명이 같으면 col colSize를 하나씩 늘린다.
					// 다르면 이전 프로젝트명을 cell로 생성하고 현재 프로젝트명으로 변경, colSize 초기화
					String projectName = null;
					int colSize = 0;
					for (String columnName : meta.getColumnNames()) {
						String name = columnName.replaceAll("'", "");
						if (!name.chars().allMatch(Character::isDigit)) { // 계정컬럼인지 체크
							continue;
						}

						if (accMap.get(name).equals(projectName)) {
							colSize++;
							continue;
						}
						if (colSize != 0) {
							h1.add(new ExcelCell(projectName, 1, colSize, HEADER));
						}
						projectName = accMap.get(name);
						colSize = 1;
					}
					h1.add(new ExcelCell(projectName, 1, colSize, HEADER));
					h1.add(new ExcelCell("Total", 2, 1, HEADER));

					// 2번쨰 헤더
					List<String> ignoreFields = List.of("ITEMTYPE", "DISPLAYORDER", "BIZTOTAL");
					List<ExcelCell> h2 = Arrays.stream(meta.getColumnNames())
							.map(name -> name.replaceAll("'", ""))
							.filter(name -> !ignoreFields.contains(name))
							.map(name -> new ExcelCell(name, HEADER))
							.collect(Collectors.toList());
					return List.of(h1, h2);
				})
				// Body
				.addRows(rs, metaData, (r, meta) -> {
					List<List<ExcelCell>> rows = new ArrayList<>();
					List<ExcelCell> row;
					int columnCount = meta.getColumnCount();
					BigDecimal usageTotCost = BigDecimal.ZERO;

					while (r.next()) {
						row = new ArrayList<>();
						// PRODUCTCODE
						String code = rs.getString(2);
						row.add(new ExcelCell(LABEL_MAP.getOrDefault(code, code), getLabelCellStyle(code)));

						// CATEGORY
						if (StringUtils.equals(code, "AWS Usage")) { // Usage 값 구하기
							usageTotCost = rs.getBigDecimal(columnCount);
						}

						Object categoryValue;
						if ("Currency(Won)".equals(code)) {
							categoryValue = r.getDouble(3);
						} else if ("AWS Support".equals(r.getString(3))) {
							BigDecimal esTotCost = rs.getBigDecimal(columnCount) != null ? rs.getBigDecimal(columnCount) : BigDecimal.ZERO;
							String result = "";

							if (BigDecimal.ZERO.compareTo(usageTotCost) != 0 && esTotCost != null) {
								result = String.format("%.2f", esTotCost.divide(usageTotCost, 9, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
							}

							categoryValue = "Enterprise Supports(Usage의 " + result + "%)";
						} else {
							categoryValue = r.getString(3);
						}
						row.add(new ExcelCell(categoryValue, getCategoryCellStyle(code, r.getString(3))));

						// VALUES
						for (int i=5 ; i<columnCount ; i++) {
							double value = r.getString(i) == null ? 0.0 : r.getDouble(i);
							row.add(new ExcelCell(value, getValueCellStyle(code)));
						}

						// TOTAL
						row.add(new ExcelCell(r.getDouble(columnCount), getTotalCellStyle(code)));

						rows.add(row);
					}
					return rows;
				})
				.build();
	}

	private ExcelConfig getExcelConfig(String fileName, String[] columnNames) {
		List<Integer> accountWidths = Arrays.stream(columnNames)
				.map(name -> name.replaceAll("'", ""))
				.filter(name -> name.chars().allMatch(Character::isDigit))
				.map(n -> 18)
				.collect(Collectors.toList());
		List<Integer> widths = new ArrayList<>(List.of(25, 45, 18));
		widths.addAll(2, accountWidths);

		String dollarFormat = "$ #,##0.000";
		String wonFormat = "\\￦ #,##0";
		ExcelStyle commonStyle = ExcelStyle.builder(COMMON)
				.borders(List.of(ExcelBorder.ALL))
				.fontSize(10)
				.fontName("나눔고딕")
				.build();
		ExcelStyle headerStyle = ExcelStyle.builder(commonStyle, HEADER)
				.backgroundColor(IndexedColors.GREY_25_PERCENT.index)
				.boldFont(true)
				.align(HorizontalAlignment.CENTER)
				.fontSize(11)
				.build();
		ExcelStyle amountStyle = ExcelStyle.builder(commonStyle, AMOUNT)
				.align(HorizontalAlignment.RIGHT)
				.cellFormat(dollarFormat)
				.build();
		ExcelStyle usageStyle = ExcelStyle.builder(commonStyle, USAGE)
				.backgroundColor(IndexedColors.TAN.index)
				.build();
		ExcelStyle usageAmountStyle = ExcelStyle.builder(amountStyle, USAGE_AMOUNT)
				.backgroundColor(IndexedColors.TAN.index)
				.build();
		ExcelStyle supportStyle = ExcelStyle.builder(commonStyle, SUPPORT)
				.backgroundColor(IndexedColors.LIGHT_YELLOW.index)
				.build();
		ExcelStyle supportAmountStyle = ExcelStyle.builder(amountStyle, SUPPORT_AMOUNT)
				.backgroundColor(IndexedColors.LIGHT_YELLOW.index)
				.build();
		ExcelStyle sumCommonStyle = ExcelStyle.builder(commonStyle, SUM_COMMON)
				.backgroundColor(IndexedColors.GREY_25_PERCENT.index)
				.build();
		ExcelStyle sumAmountStyle = ExcelStyle.builder(amountStyle, SUM_AMOUNT)
				.backgroundColor(IndexedColors.GREY_25_PERCENT.index)
				.build();
		ExcelStyle sumWonAmountStyle = ExcelStyle.builder(commonStyle, SUM_WON_AMOUNT)
				.backgroundColor(IndexedColors.GREY_25_PERCENT.index)
				.cellFormat(wonFormat)
				.build();
		ExcelStyle highSumWonAmountStyle = ExcelStyle.builder(commonStyle, SUM_HIGHLIGHT_WON_AMOUNT)
				.backgroundColor(IndexedColors.LIGHT_YELLOW.index)
				.cellFormat(wonFormat)
				.build();
		ExcelStyle percentStyle = ExcelStyle.builder(commonStyle, PERCENT)
				.cellFormat("#.0#%")
				.align(HorizontalAlignment.RIGHT)
				.build();
		return ExcelConfig.builder()
				.fileName(fileName)
				.styles(commonStyle, headerStyle, amountStyle, usageStyle, usageAmountStyle, supportStyle,
						supportAmountStyle, sumCommonStyle, sumAmountStyle, sumWonAmountStyle, highSumWonAmountStyle,
						percentStyle)
				.widths(widths)
				.build();
	}

	private String getLabelCellStyle(String code) {
		if (LABEL_MAP.containsKey(code)) {
			return SUM_COMMON;
		}
		if (LABEL_MAP_BG_ROW.containsKey(code)) {
			return LABEL_MAP_BG_ROW.get(code);
		}
		return COMMON;
	}
	private String getCategoryCellStyle(String code, String value) {
		String style = getLabelCellStyle(code);
		if (value != null && value.endsWith("%")) {
			return PERCENT;
		}
		return style;
	}
	private String getValueCellStyle(String code) {
		if (LABEL_MAP.containsKey(code)) {
			return code.equals("SubTotal") ? SUM_AMOUNT : SUM_WON_AMOUNT;
		}
		if (LABEL_MAP_BG_ROW.containsKey(code)) {
			return LABEL_MAP_BG_ROW.get(code).equals(USAGE) ? USAGE_AMOUNT : SUPPORT_AMOUNT;
		}
		return AMOUNT;
	}
	private String getTotalCellStyle(String code) {
		if (LABEL_MAP.containsKey(code)) {
			return LABEL_MAP_TOTAL_YELLOW.getOrDefault(code, SUM_AMOUNT);
		}
		if (LABEL_MAP_BG_ROW.containsKey(code)) {
			return LABEL_MAP_BG_ROW.get(code).equals(USAGE) ? USAGE_AMOUNT : SUPPORT_AMOUNT;
		}
		return AMOUNT;
	}

	private final Map<String, String> LABEL_MAP = Map.of(
			"SubTotal", "합계($)",
			"Currency(Won)", "합계(￦)",
			"Total (incl VAT)", "합계(￦/VAT포함)"
	);
	private final Map<String, String> LABEL_MAP_BG_ROW = Map.of(
			"AWS Usage", USAGE,
			"AWSSupportEnterprise", SUPPORT
	);
	private final Map<String, String> LABEL_MAP_TOTAL_YELLOW = Map.of(
			"Currency(Won)", SUM_HIGHLIGHT_WON_AMOUNT,
			"Total (incl VAT)", SUM_HIGHLIGHT_WON_AMOUNT
	);

}
