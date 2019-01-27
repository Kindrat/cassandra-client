package com.github.kindrat.cassandra.client.ui.fx;

import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class EnumStringConverter<T extends Enum<T>> extends StringConverter<T> {
    private final Class<T> enumClass;

    @Override
    public String toString(T object) {
        return object.toString().toUpperCase();
    }

    @Override
    @SneakyThrows
    public T fromString(String string) {
        return Enum.valueOf(enumClass, string.toUpperCase());
    }
}
