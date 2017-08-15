package com.github.kindrat.cassandra.client.filter.fields;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.TypeCodec;

import java.util.Objects;

public class EqPredicate extends RowPredicate {

    public EqPredicate(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean test(Row row) {
        TypeCodec<?> typeCodec = getColumnCodec(row);
        Object typedValue = typeCodec.parse(getValue());
        return Objects.equals(typedValue, row.get(getField(), typeCodec));
    }
}
