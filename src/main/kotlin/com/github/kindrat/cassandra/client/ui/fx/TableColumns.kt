package com.github.kindrat.cassandra.client.ui.fx

import com.datastax.oss.driver.api.core.type.DataType
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry
import com.github.kindrat.cassandra.client.ui.fx.CellFactory.create
import com.github.kindrat.cassandra.client.util.CqlUtil.parse
import com.github.kindrat.cassandra.client.util.UIUtil.computeTextContainerWidth
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumnBase
import javafx.scene.control.Tooltip
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control.cell.ComboBoxTableCell
import javafx.stage.Window
import javafx.util.StringConverter
import org.apache.commons.lang3.EnumUtils
import java.util.function.Function

object TableColumns {
    fun <T> buildColumn(dataType: DataType, labelText: String): TableColumn<T, Any> {
        val tableColumn = TableColumn<T, Any>()
        val label = Label(labelText)
        label.tooltip = Tooltip(dataType.asCql(false, true))
        val typeCodec = CodecRegistry.DEFAULT.codecFor<Any>(dataType)
        tableColumn.cellFactory = create(typeCodec)
        tableColumn.graphic = label
        tableColumn.minWidth = computeTextContainerWidth(label.text, label.font)
        return tableColumn
    }

    @JvmStatic
    fun <T> buildCheckBoxColumn(labelText: String,
                                extractor: Function<T, ObservableValue<Boolean>>): TableColumn<T, Boolean> {
        val tableColumn = TableColumn<T, Boolean>()
        val label = Label(labelText)
        tableColumn.cellFactory = CheckBoxTableCell.forTableColumn {
            val items = tableColumn.tableView.items
            extractor.apply(items[it])
        }
        tableColumn.graphic = label
        tableColumn.minWidth = computeTextContainerWidth(label.text, label.font)
        return tableColumn
    }

    fun <T> buildColumn(values: List<DataType>, labelText: String): TableColumn<T, DataType> {
        val tableColumn = TableColumn<T, DataType>()
        val label = Label(labelText)
        tableColumn.setCellFactory {
            val cell = ComboBoxTableCell<T, DataType>(FXCollections.observableArrayList(values))
            cell.converter = object : StringConverter<DataType>() {
                override fun toString(value: DataType): String {
                    return value.asCql(true, true)
                }

                override fun fromString(string: String): DataType {
                    return parse(string)
                }
            }
            cell
        }
        tableColumn.graphic = label
        tableColumn.minWidth = computeTextContainerWidth(label.text, label.font)
        return tableColumn
    }

    fun <T, S : Enum<S>> buildColumn(enumType: Class<S>, labelText: String): TableColumn<T, S> {
        val tableColumn = TableColumn<T, S>()
        val label = Label(labelText)
        label.tooltip = Tooltip(enumType.simpleName)
        val values = EnumUtils.getEnumList(enumType)
        tableColumn.setCellFactory {
            val cell = ComboBoxTableCell<T, S>(FXCollections.observableArrayList(values))
            cell.setConverter(EnumStringConverter(enumType))
            cell
        }
        tableColumn.graphic = label
        tableColumn.minWidth = computeTextContainerWidth(label.text, label.font)
        return tableColumn
    }

    @JvmStatic
    fun <T, S> bindTableColumnWidth(column: TableColumnBase<S, T>, parent: Window, percents: Double) {
        val parentWidth = parent.widthProperty()
        val width = parentWidth.multiply(percents)
        column.prefWidth = width.doubleValue()
        column.prefWidthProperty().bind(width)
        column.minWidthProperty().bind(width)
        column.maxWidthProperty().bind(width)
    }
}