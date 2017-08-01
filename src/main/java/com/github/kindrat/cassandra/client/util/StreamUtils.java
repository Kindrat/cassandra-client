package com.github.kindrat.cassandra.client.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class StreamUtils {

    public static <K, E> Map<K, E> toMap(Collection<E> collection, Function<E, K> keyMapper) {
        return toMap(collection, keyMapper, Function.identity());
    }

    public static <K, V, E> Map<K, V> toMap(Collection<E> collection,
                                            Function<E, K> keyMapper,
                                            Function<E, V> valueMapper) {
        if (collection == null) {
            return null;
        }
        return collection.stream().collect(Collectors.toMap(keyMapper, valueMapper));
    }
}
