package com.github.kindrat.cassandra.client.filter.fields

import com.github.kindrat.cassandra.client.ui.DataObject

class EqPredicate(field: String, value: String) : RowPredicate(field, value) {

    override fun test(data: DataObject): Boolean {
        val typeCodec = getColumnCodec(data[field])
        val typedValue = typeCodec.parse(value)
        return typedValue == data[field]
    }
}