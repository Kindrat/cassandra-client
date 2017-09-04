package com.github.kindrat.cassandra.client.filter.fields;

import com.datastax.driver.core.TypeCodec;
import com.github.kindrat.cassandra.client.ui.DataObject;

import java.util.Objects;

public class EqPredicate extends RowPredicate {

    public EqPredicate(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean test(DataObject data) {
        TypeCodec<?> typeCodec = getColumnCodec(data.get(getField()));
        Object typedValue = typeCodec.parse(getValue());
        return Objects.equals(typedValue, data.get(getField()));
    }
}
