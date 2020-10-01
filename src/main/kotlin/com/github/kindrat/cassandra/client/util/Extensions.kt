package com.github.kindrat.cassandra.client.util

import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumnBase

fun <S, T> TableColumnBase<S, T>.getCellData(index: Int, junk: Any): T? {
    if (index < 0) {
        return null
    }

    // Get the table
    val table = (this as TableColumn<S, T>).tableView
    if (table == null || table.items == null) {
        return null
    }

    // Get the rowData
    val items: List<S> = table.items
    if (index >= items.size) {
        return null
    } // Out of range

    val rowData = items[index]
    return getCellObservableValue(rowData).value
}

fun MenuBar.menu(id: String): Menu? {
    return menus.find { menu -> menu.id == id }
}