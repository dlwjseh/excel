package my.example.excel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import my.example.excel.domain.DataQuery;
import my.example.excel.repository.DataQueryRepository;

@Service
@RequiredArgsConstructor
public class DataService {
	private final DataQueryRepository dataQueryRepository;
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public <T> List<T> selectList(String queryKey, MapSqlParameterSource params, RowMapper<T> rowMapper) {
		DataQuery query = dataQueryRepository.findByQueryKey(queryKey).orElseThrow(() -> new IllegalStateException("Not found query key...: " + queryKey));
		String sql = query.getQuery();
		return jdbcTemplate.query(sql, params,rowMapper);
	}

	public Map<String, Object> selectOne(String queryKey, MapSqlParameterSource params) {
		DataQuery query = dataQueryRepository.findByQueryKey(queryKey).orElseThrow(() -> new IllegalStateException("Not found query key...: " + queryKey));
		String sql = query.getQuery();
		try {
			return jdbcTemplate.queryForMap(sql, params);
		} catch (EmptyResultDataAccessException e) {
			return new HashMap<>();
		}
	}
}
