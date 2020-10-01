package com.github.kindrat.cassandra.client.ui.fx

import javafx.util.StringConverter

class EnumStringConverter<T : Enum<T>>(private val enumClass: Class<T>) : StringConverter<T>() {

    override fun toString(value: T): String {
        return value.toString().toUpperCase()
    }

    override fun fromString(string: String): T {
        return java.lang.Enum.valueOf(enumClass, string.toUpperCase())
    }
}