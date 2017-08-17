package com.github.kindrat.cassandra.client.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static java.util.Arrays.stream;

@Getter
@RequiredArgsConstructor
public enum Operator {
    EQ("="), GT(">"), GE(">="), LT("<"), LE("<="), NE("!="), LIKE("LIKE");

    private final String value;

    public static Operator fromValue(String value) {
        return stream(Operator.values())
                .filter(operator -> operator.getValue().equals(value)).findAny()
                .orElse(null);
    }
}
