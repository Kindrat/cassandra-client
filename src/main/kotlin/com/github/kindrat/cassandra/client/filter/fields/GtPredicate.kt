package com.github.kindrat.cassandra.client.filter.fields

import com.github.kindrat.cassandra.client.ui.DataObject

class GtPredicate(field: String, value: String) : RowPredicate(field, value) {
    override fun test(data: DataObject): Boolean {
        val typeCodec = getColumnCodec(data[field])
        val expected = typeCodec.parse(value)
        val actual = data[field]
        return if (expected is Comparable<*> && actual is Comparable<*>) {
            compareValues(actual, expected) > 0
        } else {
            false
        }
    }
}