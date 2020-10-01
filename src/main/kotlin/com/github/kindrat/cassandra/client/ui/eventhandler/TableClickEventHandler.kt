package com.github.kindrat.cassandra.client.ui.eventhandler

import javafx.event.EventHandler
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.input.MouseEvent

class TableClickEventHandler<T>(private val tableView: TableView<T>) : EventHandler<MouseEvent?> {

    override fun handle(event: MouseEvent?) {
        val tableViewFocusModel = tableView.focusModelProperty().get()
        val tablePosition  = tableViewFocusModel.focusedCellProperty().get()

        @Suppress("UNCHECKED_CAST")
        val tableColumn: TableColumn<T, *>? = tablePosition.tableColumn as TableColumn<T, *>?
        if (tableColumn != null) {
            tableView.edit(tablePosition.row, tableColumn)
        } else {

        }
    }
}