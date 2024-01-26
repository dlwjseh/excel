package my.example.excel.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_PRODUCTDISPLAY")
public class ProductDisplay {
	@Id
	@Column(name = "PRODUCTCODE", nullable = false, length = 200)
	private String productCode;

	@Column(name = "DISPLAYORDER", nullable = false)
	private Integer displayOrder;
}
