package com.github.kindrat.cassandra.client.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Operator {
    EQ("="), GT(">"), GE(">="), LT("<"), LE("<="), NE("!="), LIKE("LIKE");

    private final String value;
}
