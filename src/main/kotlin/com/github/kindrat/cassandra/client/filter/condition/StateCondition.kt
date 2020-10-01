package com.github.kindrat.cassandra.client.filter.condition

interface StateCondition {
    fun isCurrentState(words: Array<String>, columnNames: Set<String>): Boolean

    fun name(): State

    enum class State {
        TABLE, PARTIAL_TABLE, OPERATOR, COMBINER
    }
}