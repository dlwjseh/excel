package my.example.excel.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Getter;

@Getter
@Entity
@Table(name = "T_DATA_QUERY")
public class DataQuery {
    @Id
    private Long id;

    @Column(name = "QUERY_KEY", unique = true)
    private String queryKey;

    @Column(name = "DESCR")
    private String descr;

    @Column(name = "QUERY")
    @Lob
    private String query;

    @Column(name = "PARAMS")
    private String params;
}
