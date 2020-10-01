package com.github.kindrat.cassandra.client.filter.condition

import com.github.kindrat.cassandra.client.util.StringUtil

class OperatorCondition : StateCondition {

    override fun isCurrentState(words: Array<String>, columnNames: Set<String>): Boolean {
        val lastWord = StringUtil.lastWord(words)
        return lastWord?.let { word: String -> columnNames.any { it.equals(word, ignoreCase = true) }} ?: false
    }

    override fun name(): StateCondition.State {
        return StateCondition.State.OPERATOR
    }
}