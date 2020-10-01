package com.github.kindrat.cassandra.client.ui.keylistener

import com.github.kindrat.cassandra.client.ui.DataObject
import com.github.kindrat.cassandra.client.util.getCellData
import javafx.scene.control.TableView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import java.util.*

class TableCellCopyHandler(private val tableView: TableView<DataObject>) : Runnable {

    override fun run() {
        val clipboard = Clipboard.getSystemClipboard()
        val content = ClipboardContent()
        val row = tableView.selectionModel.selectedIndex
        val selectedCells = tableView.selectionModel.selectedCells
        if (!selectedCells.isEmpty()) {
            val tablePosition = selectedCells[0]
            val cellData: Any? = tablePosition.tableColumn.getCellData(row, row)
            content.putString(Objects.toString(cellData))
            clipboard.setContent(content)
        }
    }
}