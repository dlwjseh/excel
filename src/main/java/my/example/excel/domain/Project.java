package my.example.excel.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Getter;

@Entity
@Getter
@Table(name = "t_project")
public class Project {

	@Id
	@Column(name = "projectid", nullable = false, updatable = false)
	private Long id;

	@Column(name = "companyid", length = 45, nullable = false)
	private String companyId;

	@Column(name = "businessid", nullable = false)
	private Long businessId;

	@Column(name = "projectname", nullable = false)
	private String projectName;

	@Column(name = "displayorder", length = 100)
	private String displayOrder;

	@Column(name = "projectdescr")
	private String projectDescription;

	@OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
	private List<ProjectAccount> projectAccounts = new ArrayList<>();

	@CreatedDate
	@Column(name = "createtime", nullable = false)
	private LocalDateTime createdDate;

	@LastModifiedDate
	@Column(name = "updatetime")
	private LocalDateTime updatedDate;

	@Column(name = "lastmodifier", length = 50, nullable = false)
	private String lastModifier;

}
