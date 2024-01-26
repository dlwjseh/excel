package my.example.excel.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import my.example.excel.enums.PartnerType;

@Entity
@Table(name = "T_INVOICEITEM")
public class InvoiceItem {
	@Id
	@Column(name = "INVOICEITEMID", nullable = false, updatable = false)
	private Long id;

	@Column(name = "USAGEYYMM", length = 6, nullable = false)
	private String usageMonth;

	@Column(name = "INVOICEYYMM", length = 6, nullable = false)
	private String invoiceMonth;

	@Column(name = "INVOICEID", length = 20, nullable = false)
	private String invoiceId;

	@Column(name = "INVOICELINEID", length = 50)
	private String invoiceLineId;

	@Enumerated(EnumType.STRING)
	@Column(name = "PARTNERTYPE", length = 10)
	private PartnerType partnerType;

	@Column(name = "RECORDTYPE", length = 4)
	private String recordType;

	@Column(name = "ITEMTYPE", length = 20, nullable = false)
	private String itemType;

	@Column(name = "CSPID", length = 10, nullable = false)
	private String cspId;

	@Column(name = "COMPANYID", length = 10)
	private String companyId;

	@Column(name = "BUSINESSID", nullable = false)
	private Long businessId;

	@Column(name = "INVOICEITEM", length = 20)
	private String invoiceItem;

	@Column(name = "INVOICEITEMNAME", length = 200)
	private String invoiceItemName;

	@Column(name = "INVOICESUBITEM", length = 12)
	private String invoiceSubItem;

	@Column(name = "INVOICESUBITEMNAME", length = 200)
	private String invoiceSubItemName;

	@Column(name = "PROJECTID")
	private Long projectId;

	@Column(name = "PRODUCTCODE", length = 100)
	private String productCode;

	@Column(name = "PRODUCTCATEGORY", length = 100)
	private String productCategory;

	@Column(name = "BASECOST", columnDefinition = "NUMBER(38,16)")
	private BigDecimal baseCost;

	@Column(name = "ADDITIONALCOST", columnDefinition = "NUMBER(38,16)")
	private BigDecimal additionalCost;

	@Column(name = "APPLYRATE", columnDefinition = "NUMBER(38,16)")
	private BigDecimal applyRate;

	@Column(name = "APPLYEDCOST", columnDefinition = "NUMBER(38,16)")
	private BigDecimal appliedCost;

	@Column(name = "MSPCOST", columnDefinition = "NUMBER(38,16)")
	private BigDecimal mspCost;

	@Column(name = "CURRENCYRATE")
	private BigDecimal currencyRate;

	@Column(name = "MSPKCOST", columnDefinition = "NUMBER(38,16)")
	private BigDecimal mspKCost;

	@Column(name = "MSPKPARTNERCOST", columnDefinition = "NUMBER(38,16)")
	private BigDecimal mspKPartnerCost;

	@Column(name = "ACCUMUSAGEID")
	private Long accumUsageId;

	@Column(name = "ACCUMTYPE", length = 10, nullable = false)
	private String accumType;

	@Column(name = "TAGTYPE", length = 20)
	private String tagType;

	@CreatedDate
	@Column(name = "CREATETIME", nullable = false)
	private LocalDateTime createTime;

	@LastModifiedDate
	@Column(name = "UPDATETIME")
	private LocalDateTime updatedDate;

	@Column(name = "LASTMODIFIER", length = 50, nullable = false)
	private String lastModifier;

}
