package com.github.kindrat.cassandra.client.ui.window.editor.main.table

import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata
import com.github.kindrat.cassandra.client.ui.DataObject
import com.github.kindrat.cassandra.client.ui.eventhandler.TableClickEventHandler
import com.github.kindrat.cassandra.client.ui.keylistener.TableCellCopyHandler
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.GridPane

class DataTableView : TableView<DataObject>() {
    fun setDataColumns(columns: List<TableColumn<DataObject, Any>>) {
        getColumns().clear()
        items.clear()
        isEditable = true
        getColumns().addAll(columns)
    }

    fun onTableSelected(tableMetadata: TableMetadata) {
        onMouseClicked = TableClickEventHandler(this)
    }

    fun buildCopyHandler(): TableCellCopyHandler {
        return TableCellCopyHandler(this)
    }

    init {
        isEditable = true
        setPrefSize(200.0, 200.0)
        GridPane.setHalignment(this, HPos.CENTER)
        GridPane.setValignment(this, VPos.CENTER)
        GridPane.setRowIndex(this, 1)
        columnResizePolicy = UNCONSTRAINED_RESIZE_POLICY
        val selectionModel = selectionModel
        selectionModel.selectionMode = SelectionMode.SINGLE
        selectionModel.isCellSelectionEnabled = true
    }
}