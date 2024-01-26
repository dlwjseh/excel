package my.example.excel;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import my.example.excel.domain.Product;
import my.example.excel.domain.ProductUsage;
import my.example.excel.vo.AccountVo;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.junit.jupiter.api.Test;

@Sql("classpath:init.sql")
@DataJpaTest
@TestPropertySource("classpath:application-test.yaml")
public class ExcelTest {
	@Autowired
	private EntityManager em;

	@Test
	void test() throws Exception {
		// 데이터 조회
		String sql = "SELECT pu FROM ProductUsage pu INNER JOIN FETCH pu.product";
		List<ProductUsage> productUsages = em.createQuery(sql, ProductUsage.class).getResultList();
		List<AccountVo> accounts = productUsages.stream()
				.map(pu -> new AccountVo(pu.getAccountId(), pu.getBusinessName()))
				.distinct()
				.sorted(Comparator.comparing(AccountVo::getAccountId))
				.collect(Collectors.toList());
		Map<Product, List<Double>> products = productUsages.stream()
				.sorted(Comparator.comparing(ProductUsage::getAccountId))
				.collect(Collectors.groupingBy(ProductUsage::getProduct,
						Collectors.mapping(ProductUsage::getAmount, Collectors.toList())));

		double awsUsageDiscountRate = 18.05;
		double supportRate = 5.37;
		double supportDiscountRate = 20;
		double wonExchangeRate = 1301.9;
		double companyDistributionRate = 95.226;

		// 엑셀 설정
		ExcelConfig config = getExcelConfig();

		// 엑셀 그리기
		ExcelWriter.builder(config)
				// Header
				.addRows(() -> {
					List<ExcelCell> first = new ArrayList<>();
					first.add(new ExcelCell("Group", 1, 2, "Header"));
					for (AccountVo account : accounts) {
						first.add(new ExcelCell(account.getBusinessName(), "Header"));
					}
					first.add(new ExcelCell("Total", 2, 1, "Header"));

					List<ExcelCell> second = new ArrayList<>();
					second.add(new ExcelCell("PRODUCTCODE", "Header"));
					second.add(new ExcelCell("PRODUCTCATEGORY", "Header"));
					for (AccountVo account : accounts) {
						second.add(new ExcelCell(account.getAccountId(), "Header"));
					}

					return List.of(first, second);
				})
				// Products
				.addRows(() -> products.entrySet().stream()
						.peek(e -> {
							for (int i=0 ; i<e.getValue().size() ; i++) {
								accounts.get(i).addUsage(e.getValue().get(i));
							}
						})
						.map(e -> List.of(
								new ExcelCell(e.getKey().getName(), "Text"),
								new ExcelCell(e.getKey().getCategory(), "Text"),
								new ExcelCell(e.getValue().get(0), "Amount"),
								new ExcelCell(e.getValue().get(1), "Amount"),
								new ExcelCell(e.getValue().get(0) + e.getValue().get(1), "Amount")
						))
						.sorted(Comparator.comparing(list -> String.valueOf(list.get(0).getValue())))
						.collect(Collectors.toList()))
				// AWS Usage
				.add(() -> {
					List<ExcelCell> list = new ArrayList<>();
					list.add(new ExcelCell("AWS Usage", "Usage"));
					list.add(new ExcelCell("", "Usage"));
					for (AccountVo vo : accounts) {
						list.add(new ExcelCell(vo.getUsage(), "UsageAmount"));
					}
					double totalAwsUsage = accounts.stream().mapToDouble(AccountVo::getUsage).sum();
					list.add(new ExcelCell(totalAwsUsage, "UsageAmount"));
					return list;
				})
				// AWS Usage Discount
				.add(() -> {
					List<ExcelCell> list = new ArrayList<>();
					list.add(new ExcelCell("AWS Usage Discount", "Text"));
					list.add(new ExcelCell(awsUsageDiscountRate*0.01, "Percent"));
					for (AccountVo vo : accounts) {
						vo.calculateDiscountedUsage(awsUsageDiscountRate);
						list.add(new ExcelCell(vo.getDiscountedUsage(), "Amount"));
					}
					double totalDiscountedUsage = accounts.stream().mapToDouble(AccountVo::getDiscountedUsage).sum();
					list.add(new ExcelCell(totalDiscountedUsage, "Amount"));
					return list;
				})
				// AWS Support Enterprise
				.add(() -> {
					List<ExcelCell> list = new ArrayList<>();
					list.add(new ExcelCell("AWS Support Enterprise", "Support"));
					list.add(new ExcelCell("Enterprise Supports(Usage의 " + supportRate + "%)", "Support"));
					for (AccountVo vo : accounts) {
						vo.calculateSupportEnterprise(supportRate);
						list.add(new ExcelCell(vo.getSupportEnterprise(), "SupportAmount"));
					}
					double totalSupportEnterprise = accounts.stream().mapToDouble(AccountVo::getSupportEnterprise).sum();
					list.add(new ExcelCell(totalSupportEnterprise, "SupportAmount"));
					return list;
				})
				// AWS Support Discount
				.add(() -> {
					List<ExcelCell> list = new ArrayList<>();
					list.add(new ExcelCell("AWS Support Discount", "Text"));
					list.add(new ExcelCell(supportDiscountRate*0.01, "Percent"));
					for (AccountVo vo : accounts) {
						vo.calculateSupportDiscount(supportDiscountRate);
						list.add(new ExcelCell(vo.getSupportDiscount(), "Amount"));
					}
					double totalSupportDiscount = accounts.stream().mapToDouble(AccountVo::getSupportDiscount).sum();
					list.add(new ExcelCell(totalSupportDiscount, "Amount"));
					return list;
				})
				// Sum
				.addRows(() -> {
					// 합계($)
					List<ExcelCell> dollarSum = new ArrayList<>(List.of(new ExcelCell("합계($)", "SumText"), new ExcelCell("", "SumText")));
					for (AccountVo vo : accounts) {
						dollarSum.add(new ExcelCell(vo.getDollarSum(), "SumAmount"));
					}
					double totalDollarSum = accounts.stream().mapToDouble(AccountVo::getDollarSum).sum();
					dollarSum.add(new ExcelCell(totalDollarSum, "SumAmount"));

					// 합계(￦)
					List<ExcelCell> wonSum = new ArrayList<>(List.of(new ExcelCell("합계(￦)", "SumText"), new ExcelCell("", "SumText")));
					for (AccountVo vo : accounts) {
						vo.calculateWonSum(wonExchangeRate);
						wonSum.add(new ExcelCell(vo.getWonSum(), "SumWonAmount"));
					}
					double totalWonSum = accounts.stream().mapToDouble(AccountVo::getWonSum).sum();
					wonSum.add(new ExcelCell(totalWonSum, "HighSumWonAmount"));

					// 합계(￦/VAT포함)
					List<ExcelCell> wonVatSum = new ArrayList<>(List.of(new ExcelCell("합계(￦/VAT포함)", "SumText"), new ExcelCell("", "SumText")));
					for (AccountVo vo : accounts) {
						wonVatSum.add(new ExcelCell(vo.getWonSumWithVat(), "SumWonAmount"));
					}
					double totalVatWonSum = accounts.stream().mapToDouble(AccountVo::getWonSumWithVat).sum();
					wonVatSum.add(new ExcelCell(totalVatWonSum, "HighSumWonAmount"));

					List<ExcelCell> companyDistribution = List.of(
							new ExcelCell("계열사별 배분금액(￦/VAT별도)", "SumText"),
							new ExcelCell("", "SumText"),
							new ExcelCell(companyDistributionRate*0.01, 2, 2, "Percent"),
							new ExcelCell(totalWonSum * companyDistributionRate * 0.01, "HighSumWonAmount")
					);

					List<ExcelCell> companyDistributionVat = List.of(
							new ExcelCell("계열사별 배분금액(￦/VAT포함)", "SumText"),
							new ExcelCell("", "SumText"),
							new ExcelCell(totalVatWonSum * companyDistributionRate * 0.01, "HighSumWonAmount")
					);

					return List.of(dollarSum, wonSum, wonVatSum, companyDistribution, companyDistributionVat);
				})
				.write(new FileOutputStream("/temp/test.xlsx"));
	}

	private static ExcelConfig getExcelConfig() {
		String dollarFormat = "$ #,##0.000";
		String wonFormat = "\\￦ #,##0";
		ExcelStyle textStyle = ExcelStyle.builder("Text")
				.borders(List.of(ExcelBorder.ALL))
				.build();
		ExcelStyle headerStyle = ExcelStyle.builder("Header")
				.backgroundColor(IndexedColors.GREY_25_PERCENT.index)
				.boldFont(true)
				.align(HorizontalAlignment.CENTER)
				.borders(List.of(ExcelBorder.ALL))
				.build();
		ExcelStyle amountStyle = ExcelStyle.builder("Amount")
				.align(HorizontalAlignment.RIGHT)
				.cellFormat(dollarFormat)
				.borders(List.of(ExcelBorder.ALL))
				.build();
		ExcelStyle usageStyle = ExcelStyle.builder("Usage")
				.backgroundColor(IndexedColors.TAN.index)
				.borders(List.of(ExcelBorder.ALL))
				.build();
		ExcelStyle usageAmountStyle = ExcelStyle.builder("UsageAmount")
				.backgroundColor(IndexedColors.TAN.index)
				.borders(List.of(ExcelBorder.ALL))
				.cellFormat(dollarFormat)
				.build();
		ExcelStyle supportStyle = ExcelStyle.builder("Support")
				.backgroundColor(IndexedColors.LIGHT_YELLOW.index)
				.borders(List.of(ExcelBorder.ALL))
				.build();
		ExcelStyle supportAmountStyle = ExcelStyle.builder("SupportAmount")
				.backgroundColor(IndexedColors.LIGHT_YELLOW.index)
				.borders(List.of(ExcelBorder.ALL))
				.cellFormat(dollarFormat)
				.build();
		ExcelStyle sumTextStyle = ExcelStyle.builder("SumText")
				.backgroundColor(IndexedColors.GREY_25_PERCENT.index)
				.borders(List.of(ExcelBorder.ALL))
				.build();
		ExcelStyle sumAmountStyle = ExcelStyle.builder("SumAmount")
				.backgroundColor(IndexedColors.GREY_25_PERCENT.index)
				.borders(List.of(ExcelBorder.ALL))
				.cellFormat(dollarFormat)
				.build();
		ExcelStyle sumWonAmountStyle = ExcelStyle.builder("SumWonAmount")
				.backgroundColor(IndexedColors.GREY_25_PERCENT.index)
				.borders(List.of(ExcelBorder.ALL))
				.cellFormat(wonFormat)
				.build();
		ExcelStyle highSumWonAmountStyle = ExcelStyle.builder("HighSumWonAmount")
				.backgroundColor(IndexedColors.YELLOW.index)
				.borders(List.of(ExcelBorder.ALL))
				.cellFormat(wonFormat)
				.build();
		ExcelStyle percentStyle = ExcelStyle.builder("Percent")
				.borders(List.of(ExcelBorder.ALL))
				.cellFormat("#.0#%")
				.align(HorizontalAlignment.RIGHT)
				.build();
		return ExcelConfig.builder()
				.styles(textStyle, headerStyle, amountStyle, usageStyle, usageAmountStyle, supportStyle,
						supportAmountStyle, sumTextStyle, sumAmountStyle, sumWonAmountStyle, highSumWonAmountStyle,
						percentStyle)
				.widths(30, 57, 24, 24, 13)
				.build();
	}

}
