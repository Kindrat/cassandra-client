package com.github.kindrat.cassandra.client.ui;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class DataObject {
    @Getter
    private final int position;
    private final Map<String, Object> data = new HashMap<>();

    public void set(String name, Object value) {
        data.put(name, value);
    }

    public Object get(String name) {
        if ("#".equals(name)) {
            return position;
        }
        return data.get(name);
    }
}
