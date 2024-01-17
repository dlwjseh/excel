package my.example.excel;

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

		// 엑셀 설정
		ExcelStyle headerStyle = ExcelStyle.builder()
				.styleName("Header")
				.backgroundColor(IndexedColors.GREY_25_PERCENT.index)
				.boldFont(true)
				.align(HorizontalAlignment.CENTER)
				.borders(List.of(ExcelBorder.ALL))
				.build();
		ExcelStyle amountStyle = ExcelStyle.builder()
				.styleName("Amount")
				.align(HorizontalAlignment.RIGHT)
				.cellFormat("$ #,##0.000")
				.build();
		ExcelConfig config = ExcelConfig.builder()
				.styles(headerStyle, amountStyle)
				.widths(30, 57, 22, 22, 13)
				.build();

		// 엑셀 그리기
		ExcelWriter.builder(config)
				// 1
				.add(() -> {
					List<ExcelCell> list = new ArrayList<>();
					list.add(new ExcelCell("Group", 1, 2, "Header"));
					for (AccountVo account : accounts) {
						list.add(new ExcelCell(account.getBusinessName(), "Header"));
					}
					list.add(new ExcelCell("Total", 2, 1, "Header"));
					return list;
				})
				// 2
				.add(() -> {
					List<Object> values = new ArrayList<>();
					values.add("PRODUCTCODE");
					values.add("PRODUCTCATEGORY");
					for (AccountVo account : accounts) {
						values.add(account.getAccountId());
					}
					return values;
				}, "Header")
				// 3
				.addRows(() -> products.entrySet().stream()
						.map(e -> List.of(
								new ExcelCell(e.getKey().getName()),
								new ExcelCell(e.getKey().getCategory()),
								new ExcelCell(e.getValue().get(0), "Amount"),
								new ExcelCell(e.getValue().get(1), "Amount"),
								new ExcelCell(e.getValue().get(0) + e.getValue().get(1), "Amount")
						))
						.sorted(Comparator.comparing(list -> String.valueOf(list.get(0).getValue())))
						.collect(Collectors.toList()))
				.write();
	}

}
