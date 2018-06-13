package com.github.kindrat.cassandra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("data")
public class TestEntity implements Serializable {
    @PrimaryKey
    private Integer counter;
    private Long time;
    private String data;
}
