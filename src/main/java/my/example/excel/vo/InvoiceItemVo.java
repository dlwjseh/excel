package my.example.excel.vo;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class InvoiceItemVo {

	private String invoiceItem;

	private String itemType;

	private String productCode;

	private String productCategory;

	private BigDecimal appliedCost;

	private Integer displayOrder;

}
