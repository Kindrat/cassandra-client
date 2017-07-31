package com.github.kindrat.cassandra.client.filter.fields;

import com.datastax.driver.core.Row;

import java.util.function.Predicate;

public abstract class RowPredicate implements Predicate<Row> {
}
