package com.github.kindrat.cassandra.client.ui.window.editor.main

import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata
import com.github.kindrat.cassandra.client.service.TableContext
import com.github.kindrat.cassandra.client.ui.DataObject
import com.github.kindrat.cassandra.client.ui.keylistener.TableCellCopyHandler
import com.github.kindrat.cassandra.client.ui.window.editor.main.filter.FilterGrid
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.DataTableView
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.PaginationPanel
import com.github.kindrat.cassandra.client.util.UIUtil.fillParent
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control.TableColumn
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import java.util.function.Consumer

class TableDataGridPane(
        private val filterGrid: FilterGrid,
        private val dataTableView: DataTableView,
        private val paginationPanel: PaginationPanel
) : GridPane() {
    private val processor = EmitterProcessor.create<DataObject>()
    private val sink = processor.sink()

    fun updateTableMetadata(metadata: TableMetadata?) {
        dataTableView.onTableSelected(metadata!!)
        filterGrid.setTableMetadata(metadata)
    }

    fun setDataColumns(columns: List<TableColumn<DataObject, Any>>) {
        dataTableView.setDataColumns(columns)
    }

    fun setData(context: TableContext?, data: ObservableList<DataObject>) {
        dataTableView.items = data
        dataTableView.refresh()
        filterGrid.onDataUpdated(data)
        dataTableView.onKeyPressed = EventHandler { event: KeyEvent ->
            if (event.code == KeyCode.DELETE) {
                val selectionModel = dataTableView.selectionModel
                val selectedItem = selectionModel.selectedItem
                val selectedIndex = selectionModel.selectedIndex
                data.removeAt(selectedIndex)
                sink.next(selectedItem!!)
            }
        }
        paginationPanel.applyOnTable(context!!, Consumer {
            Platform.runLater {
                dataTableView.items = it
                dataTableView.refresh()
            }
        })
        isVisible = true
    }

    fun objectsToRemoveStream(): Flux<DataObject> {
        return processor
    }

    fun buildCopyHandler(): TableCellCopyHandler {
        return dataTableView.buildCopyHandler()
    }

    fun suggestCompletion() {
        filterGrid.suggestCompletion()
    }

    init {
        layoutX = 78.0
        layoutY = 131.0
        isVisible = false
        val column = ColumnConstraints()
        column.hgrow = Priority.SOMETIMES
        column.minWidth = 10.0
        column.prefWidth = 100.0
        columnConstraints.clear()
        columnConstraints.add(column)
        val row1 = RowConstraints()
        row1.maxHeight = 40.0
        row1.minHeight = 40.0
        row1.prefHeight = 40.0
        row1.vgrow = Priority.SOMETIMES
        val row2 = RowConstraints()
        row2.vgrow = Priority.SOMETIMES
        val row3 = RowConstraints()
        row3.maxHeight = 40.0
        row3.minHeight = 40.0
        row3.prefHeight = 40.0
        row3.vgrow = Priority.SOMETIMES
        rowConstraints.clear()
        rowConstraints.addAll(row1, row2, row3)
        add(filterGrid, 0, 0)
        add(dataTableView, 0, 1)
        add(paginationPanel, 0, 2)
        fillParent(this)
    }
}