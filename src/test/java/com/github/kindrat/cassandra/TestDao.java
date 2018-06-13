package com.github.kindrat.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestDao extends CassandraRepository<TestEntity, UUID> {
}
