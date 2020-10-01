package com.github.kindrat.cassandra.client.ui.eventhandler

import com.github.kindrat.cassandra.client.filter.DataFilter.parsePredicate
import com.github.kindrat.cassandra.client.ui.DataObject
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import kotlin.streams.toList

class FilterBtnHandler(
        private val filterTb: TextField,
        private val dataTable: TableView<DataObject>,
        private val originalData: ObservableList<DataObject>
) : EventHandler<ActionEvent?> {

    override fun handle(event: ActionEvent?) {
        if (filterTb.text.isEmpty()) {
            if (dataTable.items.size != originalData.size) {
                dataTable.items = originalData
            }
        } else {
            val rowPredicate = parsePredicate(filterTb.text)
            val filtered = originalData.stream().filter(rowPredicate).toList()
            dataTable.setItems(FXCollections.observableArrayList(filtered))
        }
    }
}