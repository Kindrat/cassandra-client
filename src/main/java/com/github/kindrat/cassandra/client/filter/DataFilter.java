package com.github.kindrat.cassandra.client.filter;

import com.datastax.driver.core.Row;
import com.github.kindrat.cassandra.client.filter.fields.EqPredicate;
import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Builder
public class DataFilter implements Predicate<Row> {
    @Singular
    private List<Predicate<Row>> predicates;

    public static Optional<DataFilter> parse(String filterString) {
        try {
            String[] values = filterString.split(Operator.EQ.getValue());
            return Optional.of(DataFilter.builder()
                    .predicate(new EqPredicate<>(String.class, values[0], values[1]))
                    .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean test(Row row) {
        return predicates.stream()
                .anyMatch(fieldPredicate -> fieldPredicate.test(row));
    }
}
