package my.example.excel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import my.example.excel.domain.Partner;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

    List<Partner> findAllByOrderByIdAsc();

    Partner findByPartnerCd(String partnerCd);
}
