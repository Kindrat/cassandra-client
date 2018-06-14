package com.github.kindrat.cassandra.client.filter.condition;

import java.util.Set;

public interface StateCondition {
    boolean isCurrentState(String[] words, Set<String> columnNames);

    State name();

    enum State {
        TABLE, PARTIAL_TABLE, OPERATOR, COMBINER
    }
}
