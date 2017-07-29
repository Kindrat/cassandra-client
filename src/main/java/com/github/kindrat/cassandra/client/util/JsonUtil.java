package com.github.kindrat.cassandra.client.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public static String toJson(Object pojo) {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo);
    }
}
