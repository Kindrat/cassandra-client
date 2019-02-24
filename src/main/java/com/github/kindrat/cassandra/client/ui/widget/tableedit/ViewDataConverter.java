package com.github.kindrat.cassandra.client.ui.widget.tableedit;

import com.datastax.driver.core.DataType;
import com.github.kindrat.cassandra.client.util.CqlUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.stream;

class ViewDataConverter {
    private static final Pattern TABLE_PATTERN;
    private static final Pattern ROW_PATTERN;
    private static final Pattern PRIMARY_KEY_PATTERN;

    static {
        TABLE_PATTERN = Pattern.compile("(?i)(create table)(\\s+)(.*)(\\s+)(\\()([.\\s\\S]*)(\\))");

        List<String> types = stream(DataType.Name.values()).map(Enum::toString).collect(Collectors.toList());
        String combinedTypePattern = String.join("|", types);
        ROW_PATTERN = Pattern.compile(format("(?i)([\"\\-0-9a-z]+)(\\s+)(%s)(\\s*)", combinedTypePattern));

        PRIMARY_KEY_PATTERN = Pattern.compile("(?i)(primary key \\()(.*)(\\))");
    }

    Map.Entry<String, Collection<TableRowEntry>> fromText(String text) {
        Matcher matcher = TABLE_PATTERN.matcher(text);
        boolean tableValid = matcher.find();
        if (tableValid) {
            String tableName = matcher.group(3);
            String rows = matcher.group(6);
            String[] separateRawRows = stream(rows.split("\n"))
                    .map(String::trim)
                    .map(str -> StringUtils.removeEnd(str, ","))
                    .toArray(String[]::new);

            Map<String, TableRowEntry> tableRowEntries = stream(separateRawRows)
                    .filter(row -> ROW_PATTERN.matcher(row).find())
                    .map(this::parseRow)
                    .collect(Collectors.toMap(TableRowEntry::getName, Function.identity()));

            stream(separateRawRows)
                    .filter(row -> PRIMARY_KEY_PATTERN.matcher(row).find())
                    .findAny()
                    .ifPresent(row -> {
                        Matcher pkMatcher = PRIMARY_KEY_PATTERN.matcher(row);
                        boolean isPkLine = pkMatcher.find();
                        if (isPkLine) {
                            String nameLine = pkMatcher.group(2);
                            String[] names = nameLine.split(",");
                            String partitionKey = names[0].trim();
                            tableRowEntries.get(partitionKey).setIsPartitionKey(true);
                            if (names.length > 1) {
                                for (int i = 1; i < names.length; i++) {
                                    String clusteringKey = names[i].trim();
                                    tableRowEntries.get(clusteringKey).setIsClusteringKey(true);
                                }
                            }
                        }
                    });

            return new AbstractMap.SimpleEntry<>(tableName, tableRowEntries.values());
        }
        return new AbstractMap.SimpleEntry<>("", Collections.emptyList());
    }

    private TableRowEntry parseRow(String rawRow) {
        Matcher matcher = ROW_PATTERN.matcher(rawRow);
        boolean matches = matcher.find();
        if (matches) {
            String name = matcher.group(1);
            String type = matcher.group(3);
            DataType.Name dataType = DataType.Name.valueOf(type.toUpperCase());
            return new TableRowEntry(name, dataType, false, false, false);
        } else {
            throw new IllegalStateException("Regex match should happens before");
        }
    }

    String toText(List<TableRowEntry> rows, String table) {
        List<String> rowDefinitions = rows.stream()
                .map(tableRowEntry -> format("%s %s", tableRowEntry.getName(), tableRowEntry.getType()))
                .collect(Collectors.toList());

        rows.stream()
                .filter(entry -> entry.isClusteringKey() || entry.isPartitionKey())
                .sorted(Comparator.comparing(TableRowEntry::isPartitionKey).reversed())
                .map(TableRowEntry::getName)
                .reduce((s, s2) -> s + ", " + s2)
                .ifPresent(s -> rowDefinitions.add(String.format("PRIMARY KEY (%s)", s)));

        String tableRows = rowDefinitions.stream().reduce((s, s2) -> s + ", " + s2).orElse("");
        String tableRawDDL = String.format("CREATE TABLE %s (%s)", table, tableRows);
        return CqlUtil.formatDDL(tableRawDDL);
    }
}
