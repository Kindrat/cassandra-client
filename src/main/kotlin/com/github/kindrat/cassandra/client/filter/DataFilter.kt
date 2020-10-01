package com.github.kindrat.cassandra.client.filter

import com.github.kindrat.cassandra.client.filter.fields.*
import com.github.kindrat.cassandra.client.ui.DataObject
import com.github.kindrat.cassandra.client.util.StringUtil
import com.github.kindrat.cassandra.client.util.UIUtil
import java.util.*
import java.util.function.Predicate

object DataFilter {
    private val predicateFactories: MutableMap<Operator, PredicateProvider> = EnumMap(Operator::class.java)

    @JvmStatic
    fun parsePredicate(filter: String): Predicate<DataObject> {
        if (filter.startsWith("(")) {
            val closingBracketIndex = StringUtil.findClosingBracketIndex(filter)
            val predicate = parsePredicate(filter.substring(1, closingBracketIndex))
            return if (closingBracketIndex != filter.length - 1) {
                val secondFilterPart = filter.substring(closingBracketIndex + 1).trim { it <= ' ' }
                when {
                    secondFilterPart.startsWith(Combiner.AND.name, ignoreCase = true) -> {
                        predicate.and(parsePredicate(secondFilterPart.substring(3).trim { it <= ' ' }))
                    }

                    secondFilterPart.startsWith(Combiner.OR.name, ignoreCase = true) -> {
                        predicate.or(parsePredicate(secondFilterPart.substring(2).trim { it <= ' ' }))
                    }
                    else -> {
                        throw IllegalArgumentException("Illegal combiner : $secondFilterPart")
                    }
                }
            } else {
                return predicate
            }
        }
        val words = UIUtil.parseWords(filter)
        require(words.size >= 3) { "Incomplete filter : $filter" }
        val firstWord = StringUtil.wordAtPosition(words, 0) ?:
            throw IllegalArgumentException("Should not be called with empty string")
        val secondWord = StringUtil.wordAtPosition(words, 1) ?:
            throw IllegalArgumentException("Should not be called with empty string")
        val thirdWord = StringUtil.wordAtPosition(words, 2) ?:
            throw IllegalArgumentException("Should not be called with empty string")
        val operator: Operator = Operator.fromValue(secondWord)
        val operatorPredicate: Predicate<DataObject> = predicateFactories[operator]!!.build(firstWord, thirdWord)
        val fourthWord = StringUtil.wordAtPosition(words, 3)
        fourthWord?.let {
            val combiner = Combiner.valueOf(it)
            val remainingWords = words.copyOfRange(4, words.size)
            val remainingFilterString = remainingWords.joinToString { " " }
            return when (combiner) {
                Combiner.AND -> operatorPredicate.and(parsePredicate(remainingFilterString))
                Combiner.OR -> operatorPredicate.or(parsePredicate(remainingFilterString))
            }
        }
        return operatorPredicate
    }

    fun interface PredicateProvider {
        fun build(field: String, value: String): RowPredicate
    }

    init {
        predicateFactories[Operator.EQ] = PredicateProvider { field, value -> EqPredicate(field, value) }
        predicateFactories[Operator.GT] = PredicateProvider { field, value -> GtPredicate(field, value) }
        predicateFactories[Operator.GE] = PredicateProvider { field, value -> GePredicate(field, value) }
        predicateFactories[Operator.LT] = PredicateProvider { field, value -> LtPredicate(field, value) }
        predicateFactories[Operator.LT] = PredicateProvider { field, value -> LePredicate(field, value) }
        predicateFactories[Operator.NE] = PredicateProvider { field, value -> NePredicate(field, value) }
        predicateFactories[Operator.LIKE] = PredicateProvider { field, value -> LikePredicate(field, value) }
    }
}