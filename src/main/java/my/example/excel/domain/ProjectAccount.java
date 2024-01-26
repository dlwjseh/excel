package my.example.excel.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Getter;
import my.example.excel.enums.YN;

@Entity
@Getter
@Table(name = "t_projectaccount")
public class ProjectAccount {
	@Id
	@Column(name = "projectaccountid", nullable = false, updatable = false)
	private Long id;

	@Column(name = "cspid", length = 10, nullable = false)
	private String cspId;

	@Column(name = "companyid", length = 45, nullable = false)
	private String companyId;

	@Column(name = "businessid", nullable = false)
	private Long businessId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "projectid", nullable = false)
	private Project project;

	@Column(name = "payaccountid", nullable = false)
	private String payAccountId;

	@Column(name = "accountid", nullable = false)
	private String accountId;

	@Column(name = "tagname", nullable = false)
	private String tagName;

	@Column(name = "accountclassid", length = 100)
	private String accountClassId;

	@Column(name = "envtype", length = 10)
	private String envType;

	@Column(name = "displayorder")
	private Integer displayOrder;

	@Column(name = "displaygroupname", length = 100)
	private String displayGroupName;

	@Column(name = "startdate", nullable = false)
	private LocalDateTime startDate;

	@CreatedDate
	@Column(name = "CREATETIME", nullable = false)
	private LocalDateTime createTime;

	@LastModifiedDate
	@Column(name = "UPDATETIME")
	private LocalDateTime updatedDate;

	@Column(name = "LASTMODIFIER", length = 50, nullable = false)
	private String lastModifier;

	private String awsProfile;

	@Enumerated(EnumType.STRING)
	@Column(length = 1)
	private YN useYn = YN.Y;

	private String serviceCode;
}
