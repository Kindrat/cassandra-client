package com.github.kindrat.cassandra.client.ui.fx

import com.datastax.oss.driver.api.core.type.codec.TypeCodec
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback
import javafx.util.StringConverter
import org.apache.commons.lang3.StringUtils

object CellFactory {
    fun <S, T> create(codec: TypeCodec<T>): Callback<TableColumn<S, T>, TableCell<S, T>> {
        return TextFieldTableCell.forTableColumn(object : StringConverter<T>() {
            override fun toString(value: T?): String {
                return codec.format(value)
            }

            override fun fromString(string: String): T? {
                return codec.parse(StringUtils.wrapIfMissing(string, "'"))
            }
        })
    }
}