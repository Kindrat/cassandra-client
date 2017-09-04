package com.github.kindrat.cassandra.client.filter.fields;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.TypeCodec;
import com.github.kindrat.cassandra.client.ui.DataObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

import static lombok.AccessLevel.PROTECTED;

@Getter(PROTECTED)
@RequiredArgsConstructor
public abstract class RowPredicate implements Predicate<DataObject> {
    @Getter(PROTECTED)
    private static final CodecRegistry codecRegistry = new CodecRegistry();
    private final String field;
    private final String value;

    TypeCodec<?> getColumnCodec(Object actual) {
        return getCodecRegistry().codecFor(actual);
    }
}
