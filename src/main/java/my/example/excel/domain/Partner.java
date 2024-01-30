package my.example.excel.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_partner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partnerid")
    private Long id;

    @Column(name = "partnername")
    private String partnerName;

    @Column(name = "partnerdescr")
    private String partnerDescription;

    @Column(name = "companyid")
    private Long companyId;

    @Column(name = "starttime")
    private LocalDateTime startTime;

    @Column(name = "endtime")
    private LocalDateTime endTime;

    @Column(name = "logo_code")
    private String logoCode;

    @Column(name = "PARTNERCD")
    private String partnerCd;

    @Column(name = "COMPANY_CODE")
    private String companyCode;

    @Column(name = "BILLING_YN", columnDefinition = "CHAR")
    private String billingYn;


}
