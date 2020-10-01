package com.github.kindrat.cassandra.client.util

import com.datastax.oss.driver.api.core.type.DataType
import com.datastax.oss.driver.api.core.type.DataTypes
import org.hibernate.engine.jdbc.internal.DDLFormatterImpl
import java.lang.IllegalArgumentException
import java.util.regex.Pattern

object CqlUtil {
    private val select = Pattern.compile("([sS][eE][lL][eE][cC][tT])(\\ \\S+\\ )([fF][rR][oO][mM]\\ )(\\S+)")
    private val ddlFormatter = DDLFormatterImpl.INSTANCE

    fun isSelect(cql: String): Boolean {
        return select.matcher(cql).find()
    }

    fun getSelectTable(cql: String): String? {
        val matcher = select.matcher(cql)
        return if (matcher.find()) {
            matcher.group(4)
        } else null
    }

    fun formatDDL(rawDDL: String): String {
        return ddlFormatter.format(rawDDL)
                .replace("AND".toRegex(), "\n\tAND")
    }

    fun parse(value: String): DataType {
        return PRIMITIVE_TYPES.find { it.asCql(true, true).equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unsupported primitive type $value")
    }

    val PRIMITIVE_TYPES = listOf(
            DataTypes.ASCII,
            DataTypes.BIGINT,
            DataTypes.BLOB,
            DataTypes.BOOLEAN,
            DataTypes.COUNTER,
            DataTypes.DECIMAL,
            DataTypes.DOUBLE,
            DataTypes.FLOAT,
            DataTypes.INT,
            DataTypes.TIMESTAMP,
            DataTypes.UUID,
            DataTypes.VARINT,
            DataTypes.TIMEUUID,
            DataTypes.INET,
            DataTypes.DATE,
            DataTypes.TEXT,
            DataTypes.TIME,
            DataTypes.SMALLINT,
            DataTypes.TINYINT,
            DataTypes.DURATION)
}