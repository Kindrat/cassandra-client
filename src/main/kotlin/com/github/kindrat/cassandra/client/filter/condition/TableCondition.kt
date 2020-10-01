package com.github.kindrat.cassandra.client.filter.condition

import com.github.kindrat.cassandra.client.filter.Combiner
import com.github.kindrat.cassandra.client.util.StringUtil
import org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase

class TableCondition : StateCondition {

    override fun isCurrentState(words: Array<String>, columnNames: Set<String>): Boolean {
        val lastWord = StringUtil.lastWord(words)
        return lastWord?.let { word: String -> equalsAnyIgnoreCase(word, Combiner.AND.name, Combiner.OR.name) } ?: true
    }

    override fun name(): StateCondition.State {
        return StateCondition.State.TABLE
    }
}