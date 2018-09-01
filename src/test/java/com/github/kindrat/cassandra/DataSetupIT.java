package com.github.kindrat.cassandra;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.stream.IntStream;

public class DataSetupIT extends SuiteSetup {

    @Autowired
    private TestDao dao;

    @Test
    public void deployTestData() {
        IntStream.rangeClosed(0, 20_000)
                .mapToObj(value -> new TestEntity(value, System.currentTimeMillis(), UUID.randomUUID().toString()))
                .forEach(dao::save);
    }
}
