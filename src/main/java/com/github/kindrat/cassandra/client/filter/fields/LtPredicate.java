package com.github.kindrat.cassandra.client.filter.fields;

import com.datastax.driver.core.TypeCodec;
import com.github.kindrat.cassandra.client.ui.DataObject;

public class LtPredicate extends RowPredicate {
    public LtPredicate(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean test(DataObject data) {
        TypeCodec<?> typeCodec = getColumnCodec(data.get(getField()));

        Object expected = typeCodec.parse(getValue());
        Object actual = data.get(getField());
        if (expected instanceof Comparable && actual instanceof Comparable) {
            //noinspection unchecked
            return ((Comparable) actual).compareTo(expected) < 0;
        } else {
            return false;
        }
    }
}
