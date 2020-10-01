package com.github.kindrat.cassandra.client.ui.widget.tableedit

import com.github.kindrat.cassandra.client.util.CqlUtil.PRIMITIVE_TYPES
import com.github.kindrat.cassandra.client.util.CqlUtil.formatDDL
import com.github.kindrat.cassandra.client.util.CqlUtil.parse
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.regex.Pattern

object ViewDataConverter {
    private val TABLE_PATTERN: Pattern = Pattern.compile("(?i)(create table)(\\s+)(.*)(\\s+)(\\()([.\\s\\S]*)(\\))")
    private val ROW_PATTERN: Pattern
    private val PRIMARY_KEY_PATTERN: Pattern = Pattern.compile("(?i)(primary key \\()(.*)(\\))")

    init {
        val types: List<String> = PRIMITIVE_TYPES.map{ it.toString() }.toList()
        val combinedTypePattern: String = types.joinToString { "|" }
        ROW_PATTERN = Pattern.compile("(?i)([\"\\-0-9a-z]+)(\\s+)($combinedTypePattern)(\\s*)")
    }

    fun fromText(text: String): Map.Entry<String, Collection<TableRowEntry>> {
        val matcher = TABLE_PATTERN.matcher(text)
        val tableValid = matcher.find()
        if (tableValid) {
            val tableName = matcher.group(3)
            val rows = matcher.group(6)
            val separateRawRows = rows.split("\n")
                    .map { obj: String -> obj.trim { it <= ' ' } }
                    .map { StringUtils.removeEnd(it, ",") }
                    .toList()

            val tableRowEntries: Map<String, TableRowEntry> = separateRawRows
                    .filter { ROW_PATTERN.matcher(it).find() }
                    .map { parseRow(it) }
                    .map { it.name to it }
                    .toMap()

            separateRawRows.find { PRIMARY_KEY_PATTERN.matcher(it).find() }
                    ?.let {
                        val pkMatcher = PRIMARY_KEY_PATTERN.matcher(it)
                        val isPkLine = pkMatcher.find()
                        if (isPkLine) {
                            val nameLine = pkMatcher.group(2)
                            val names = nameLine.split(",").toTypedArray()
                            val partitionKey = names[0].trim()
                            tableRowEntries[partitionKey]?.isPartitionKey = true
                            if (names.size > 1) {
                                for (i in 1 until names.size) {
                                    val clusteringKey = names[i].trim()
                                    tableRowEntries[clusteringKey]?.isClusteringKey = true
                                }
                            }
                        }
                    }
            return AbstractMap.SimpleEntry(tableName, tableRowEntries.values)
        }
        return AbstractMap.SimpleEntry<String, Collection<TableRowEntry>>("", listOf())
    }

    private fun parseRow(rawRow: String): TableRowEntry {
        val matcher = ROW_PATTERN.matcher(rawRow)
        val matches = matcher.find()
        return if (matches) {
            val name = matcher.group(1)
            val type = matcher.group(3)
            TableRowEntry(name, parse(type), isPartitionKey = false, isClusteringKey = false, hasIndex = false)
        } else {
            throw IllegalStateException("Regex match should happens before")
        }
    }

    fun toText(rows: List<TableRowEntry>, table: String): String {
        val rowDefinitions = rows.map { "${it.name} ${it.type}" }.toMutableList()
        rows.filter { it.isClusteringKey || it.isPartitionKey }
                .sortedWith(Comparator.comparing(TableRowEntry::isPartitionKey).reversed())
                .map { it.name }
                .reduce { s: String, s2: String -> "$s, $s2" }
                .apply { rowDefinitions.add("PRIMARY KEY ($this)") }

        val tableRows = rowDefinitions.reduce { s: String, s2: String -> "$s, $s2" }
        return formatDDL("CREATE TABLE $table ($tableRows)")
    }
}