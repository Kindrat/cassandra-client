package com.github.kindrat.cassandra.client.filter.fields;

import com.datastax.driver.core.Row;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class EqPredicate<T> extends RowPredicate {
    private final Class<T> type;
    private final String field;
    private final T value;

    @Override
    public boolean test(Row s) {
        return Objects.equals(Optional.ofNullable(s.getObject(field)).orElse("").toString(), value);
    }
}
