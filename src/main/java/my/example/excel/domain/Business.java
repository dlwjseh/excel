package my.example.excel.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;

@Entity
@Table(name = "T_BUSINESS")
@Getter
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BUSINESSID")
    private Long businessId;

    @Column(name = "CSPID")
    private String cspId;

    @Column(name = "COMPANYID")
    private String companyId;

    @Column(name = "BUSINESSNAME")
    private String businessName;

    @Column(name = "BUSINESSDESCR")
    private String businessDescr;

    @Column(name = "INVOICEPERIOD")
    private String invoiceperiod;

    @Column(name = "ACCUMTYPE")
    private String accumType;

//    @Column(name = "TAGPOLICY")
//    private String tagPolicy;

//    @Column(name = "BILLLEVEL")
//    private String billLevel;

    @Column(name = "DISPLAYORDER")
    private Integer displayOrder;

    @Column(name = "CREATETIME")
    private LocalDateTime createTime;

    @Column(name = "UPDATETIME")
    private LocalDateTime updateTime;

    @Column(name = "LASTMODIFIER")
    private String lastModifier;

}
