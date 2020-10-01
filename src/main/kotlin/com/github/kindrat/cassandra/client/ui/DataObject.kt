package com.github.kindrat.cassandra.client.ui

import java.util.*

data class DataObject(val position: Int) {
    private val data: MutableMap<String, Any> = HashMap()
    operator fun set(name: String, value: Any) {
        data[name] = value
    }

    operator fun get(name: String): Any {
        return if ("#" == name) {
            position
        } else data[name]!!
    }
}