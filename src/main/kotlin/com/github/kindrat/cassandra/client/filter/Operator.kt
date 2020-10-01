package com.github.kindrat.cassandra.client.filter

import java.lang.IllegalArgumentException

enum class Operator(val value: String) {
    EQ("="), GT(">"), GE(">="), LT("<"), LE("<="), NE("!="), LIKE("LIKE");

    companion object {
        fun fromValue(value: String): Operator {
            return values().find { it.value == value } ?: throw IllegalArgumentException("Invalid operator: $value")
        }
    }
}