package com.github.kindrat.cassandra.client.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
enum Operator {
    EQ("="), GT(">"), GE(">="), LT("<"), LE("<="), NE("!="), IN("IN");

    private final String value;
}
