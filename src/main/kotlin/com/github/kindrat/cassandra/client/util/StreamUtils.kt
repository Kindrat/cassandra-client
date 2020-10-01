package com.github.kindrat.cassandra.client.util

import java.util.function.Function
import java.util.stream.Collectors

object StreamUtils {
    fun <K, E> toMap(collection: Collection<E>?, keyMapper: Function<E, K>?): Map<K, E>? {
        return toMap(collection, keyMapper, Function.identity())
    }

    fun <K, V, E> toMap(collection: Collection<E>?, keyMapper: Function<E, K>?, valueMapper: Function<E, V>?): Map<K, V>? {
        return collection?.stream()?.collect(Collectors.toMap(keyMapper, valueMapper))
    }
}