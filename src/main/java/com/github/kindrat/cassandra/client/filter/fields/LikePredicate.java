package com.github.kindrat.cassandra.client.filter.fields;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.TypeCodec;

import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

public class LikePredicate extends RowPredicate {
    public LikePredicate(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean test(Row row) {
        TypeCodec<Comparable> typeCodec = getColumnCodec(row);
        String value = getValue();
        Pattern pattern = Pattern.compile(value);
        Object actualValue = row.get(getField(), typeCodec);
        //noinspection unchecked
        return nonNull(actualValue)
                && pattern.matcher(actualValue.toString()).find();
    }
}
