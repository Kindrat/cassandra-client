package com.github.kindrat.cassandra.client.filter.fields;

import com.github.kindrat.cassandra.client.ui.DataObject;

import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

public class LikePredicate extends RowPredicate {
    public LikePredicate(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean test(DataObject data) {
        String value = getValue();
        Pattern pattern = Pattern.compile(value);
        Object actualValue = data.get(getField());
        //noinspection unchecked
        return nonNull(actualValue)
                && pattern.matcher(actualValue.toString()).find();
    }
}
