package com.github.kindrat.cassandra.client.util;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

@UtilityClass
public class CqlUtil {
    private static final Pattern select = compile("([sS][eE][lL][eE][cC][tT])(\\ \\S+\\ )([fF][rR][oO][mM]\\ )(\\S+)");

    public static boolean isSelect(String cql) {
        return select.matcher(cql).find();
    }

    public static Optional<String> getSelectTable(String cql) {
        Matcher matcher = select.matcher(cql);
        if (matcher.find()) {
            return Optional.of(matcher.group(4));
        }
        return Optional.empty();
    }
}
