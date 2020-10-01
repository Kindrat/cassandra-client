package com.github.kindrat.cassandra.client.filter.fields

import com.github.kindrat.cassandra.client.ui.DataObject
import java.util.regex.Pattern

class LikePredicate(field: String, value: String) : RowPredicate(field, value) {

    override fun test(data: DataObject): Boolean {
        val pattern = Pattern.compile(value)
        val actualValue = data[field]
        return pattern.matcher(actualValue.toString()).find()
    }
}