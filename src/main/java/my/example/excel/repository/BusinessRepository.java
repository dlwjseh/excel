package my.example.excel.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import my.example.excel.domain.Business;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    Page<Business> findAllByBusinessIdIn(List<Long> businessIds, Pageable pageReq);
    Page<Business> findAllByBusinessId(Long businessId, Pageable pageReq);

    List<Business> findAllByOrderByBusinessIdAsc();

}
