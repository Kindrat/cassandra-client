package com.github.kindrat.cassandra.client.ui.window.editor.main.filter

import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata
import com.github.kindrat.cassandra.client.ui.DataObject
import com.github.kindrat.cassandra.client.ui.eventhandler.FilterBtnHandler
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.DataTableView
import javafx.collections.ObservableList
import javafx.geometry.HPos
import javafx.scene.control.Button
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints

class FilterGrid(private val dataTableView: DataTableView, private val textField: FilterTextField) : GridPane() {
    private val filterButton: Button = Button("Filter")

    fun setTableMetadata(tableMetadata: TableMetadata) {
        textField.setTableMetadata(tableMetadata)
    }

    fun suggestCompletion() {
        if (textField.isFocused) {
            textField.suggestCompletion()
        }
    }

    fun onDataUpdated(data: ObservableList<DataObject>) {
        filterButton.onAction = FilterBtnHandler(textField, dataTableView, data)
        textField.onAction = FilterBtnHandler(textField, dataTableView, data)
    }

    init {
        filterButton.isMnemonicParsing = false
        val column1 = ColumnConstraints()
        column1.prefWidth = 100.0
        column1.minWidth = 10.0
        column1.halignment = HPos.CENTER
        column1.hgrow = Priority.SOMETIMES
        val column2 = ColumnConstraints()
        column2.prefWidth = 100.0
        column2.minWidth = 100.0
        column2.maxWidth = 100.0
        column2.halignment = HPos.CENTER
        column2.hgrow = Priority.SOMETIMES
        columnConstraints.clear()
        columnConstraints.addAll(column1, column2)
        val row = RowConstraints(10.0, 30.0, 30.0)
        row.vgrow = Priority.SOMETIMES
        rowConstraints.clear()
        rowConstraints.add(row)
        add(textField, 0, 0)
        add(filterButton, 1, 0)
    }
}