package com.github.kindrat.cassandra.client.filter.fields;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.TypeCodec;

import static java.util.Objects.nonNull;

public class LePredicate extends RowPredicate {
    public LePredicate(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean test(Row row) {
        TypeCodec<Comparable> typeCodec = getColumnCodec(row);
        Comparable typedValue = typeCodec.parse(getValue());
        Comparable actualValue = row.get(getField(), typeCodec);
        //noinspection unchecked
        return nonNull(actualValue)
                && nonNull(typedValue)
                && actualValue.compareTo(typedValue) <= 0;
    }
}
