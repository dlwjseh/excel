package my.example.excel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import my.example.excel.domain.DataQuery;

@Repository
public interface DataQueryRepository extends JpaRepository<DataQuery, Long> {
	Optional<DataQuery> findByQueryKey(String queryKey);
}
