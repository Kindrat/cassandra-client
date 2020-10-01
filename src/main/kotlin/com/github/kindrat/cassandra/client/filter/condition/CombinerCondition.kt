package com.github.kindrat.cassandra.client.filter.condition

import com.github.kindrat.cassandra.client.filter.Operator
import com.github.kindrat.cassandra.client.util.StringUtil

class CombinerCondition : StateCondition {

    override fun isCurrentState(words: Array<String>, columnNames: Set<String>): Boolean {
        val secondFromHead = StringUtil.wordAtPosition(words, -2)
        return secondFromHead?.let { word: String -> Operator.values().any { it.value == word }} ?: false
    }

    override fun name(): StateCondition.State {
        return StateCondition.State.COMBINER
    }
}