package com.github.kindrat.cassandra.client.filter.fields;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.TypeCodec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

import static lombok.AccessLevel.PROTECTED;

@Getter(PROTECTED)
@RequiredArgsConstructor
public abstract class RowPredicate implements Predicate<Row> {
    @Getter(PROTECTED)
    private static final CodecRegistry codecRegistry = new CodecRegistry();
    private final String field;
    private final String value;

    TypeCodec<Comparable> getColumnCodec(Row row) {
        DataType type = row.getColumnDefinitions().getType(getField());
        return getCodecRegistry().codecFor(type);
    }
}
