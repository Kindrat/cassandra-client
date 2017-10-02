package com.github.kindrat.cassandra.client.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public static String toJson(Object pojo) {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo);
    }

    @SneakyThrows
    public static <T> List<T> fromJson(String json, Class<T> type) {
        CollectionType mapType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        return mapper.readerFor(mapType).readValue(json);
    }
}
