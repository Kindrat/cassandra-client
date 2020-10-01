package com.github.kindrat.cassandra.client.filter.condition

import com.github.kindrat.cassandra.client.util.StringUtil

class PartialTableCondition : StateCondition {

    override fun isCurrentState(words: Array<String>, columnNames: Set<String>): Boolean {
        val lastWord = StringUtil.lastWord(words)
        return lastWord?.let { word: String -> columnStartsWithWord(columnNames, word)} ?: false
    }

    override fun name(): StateCondition.State {
        return StateCondition.State.PARTIAL_TABLE
    }

    private fun columnStartsWithWord(columnNames: Set<String>, lastWord: String): Boolean {
        return columnNames.any { it.startsWith(lastWord) && it != lastWord }
    }
}