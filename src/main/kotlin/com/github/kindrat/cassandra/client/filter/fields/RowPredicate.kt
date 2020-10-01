package com.github.kindrat.cassandra.client.filter.fields

import com.datastax.oss.driver.api.core.type.codec.TypeCodec
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry
import com.github.kindrat.cassandra.client.ui.DataObject
import java.util.function.Predicate

abstract class RowPredicate(val field: String, val value: String) : Predicate<DataObject> {

    companion object {
        private val codecRegistry = CodecRegistry.DEFAULT

        fun getColumnCodec(actual: Any): TypeCodec<*> {
            return codecRegistry.codecFor(actual)
        }
    }
}