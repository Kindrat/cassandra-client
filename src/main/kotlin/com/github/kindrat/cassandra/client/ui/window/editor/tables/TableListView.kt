package com.github.kindrat.cassandra.client.ui.window.editor.tables

import com.github.kindrat.cassandra.client.util.UIUtil.fillParent
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane

internal class TableListView : AnchorPane() {
    private val tablesList: ListView<String>

    fun onMouseClick(onSelection: Runnable, onDeselection: Runnable, onDoubleClick: Runnable) {
        tablesList.onMouseClicked = EventHandler { event: MouseEvent ->
            val clickCount = event.clickCount
            val wrappedSelectionAction = wrapSelectionAction(onSelection, onDoubleClick, clickCount)
            onClick(wrappedSelectionAction, onDeselection)
        }
    }

    private fun wrapSelectionAction(onSelection: Runnable, onDoubleClick: Runnable, clickCount: Int): Runnable {
        return if (clickCount == 2) {
            Runnable {
                onSelection.run()
                onDoubleClick.run()
            }
        } else {
            onSelection
        }
    }

    val selectedTable: String?
        get() = tablesList.selectionModel.selectedItems.find { true }

    fun showTables(tables: ObservableList<String>) {
        tablesList.items = tables
    }

    fun clear() {
        tablesList.items = FXCollections.emptyObservableList()
    }

    fun onNewValueSelected(listener: ChangeListener<in String>) {
        tablesList.selectionModel.selectedItemProperty().addListener(listener)
    }

    private fun list(): ListView<String> {
        val tables = ListView(FXCollections.observableArrayList<String>())
        tables.selectionModel.selectionMode = SelectionMode.SINGLE
        tables.prefHeight = 480.0
        tables.prefWidth = 256.0
        return tables
    }

    private fun onClick(onSelection: Runnable, onDeselection: Runnable) {
        val selectedItems = tablesList.selectionModel.selectedItems
        if (selectedItems.isEmpty()) {
            onDeselection.run()
        } else {
            onSelection.run()
        }
    }

    init {
        prefHeight = 100.0
        prefWidth = 160.0
        tablesList = list()
        children.add(tablesList)
        fillParent(tablesList)
    }
}