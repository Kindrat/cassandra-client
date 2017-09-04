package com.github.kindrat.cassandra.client.ui;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class DataObject {
    private final Map<String, Object> data = new HashMap<>();

    public void set(String name, Object value) {
        data.put(name, value);
    }

    public Object get(String name) {
        return data.get(name);
    }
}
