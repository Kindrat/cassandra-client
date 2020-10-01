package com.github.kindrat.cassandra.client.ui.window.editor.main.table

import com.github.kindrat.cassandra.client.service.TableContext
import com.github.kindrat.cassandra.client.ui.DataObject
import com.github.kindrat.cassandra.client.util.UIUtil.buildButton
import com.github.kindrat.cassandra.client.util.UIUtil.disable
import com.github.kindrat.cassandra.client.util.UIUtil.enable
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.scene.control.Button
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import java.util.function.Consumer

class PaginationPanel : GridPane() {
    private val buttonPrevious: Button
    private val buttonNext: Button
    fun applyOnTable(context: TableContext, pageConsumer: Consumer<ObservableList<DataObject>>) {
        buttonNext.onAction = EventHandler {
            disable(buttonNext, buttonPrevious)
            context.nextPage()
                    .thenAccept(pageConsumer)
                    .thenRun {
                        enable(buttonPrevious)
                        if (context.hasNextPage()) {
                            enable(buttonNext)
                        }
                    }
        }
        buttonPrevious.onAction = EventHandler {
            disable(buttonNext, buttonPrevious)
            context.previousPage()
                    .thenAccept(pageConsumer)
                    .thenRun {
                        enable(buttonNext)
                        if (context.hasPreviousPage()) {
                            enable(buttonPrevious)
                        }
                    }
        }
    }

    init {
        isDisabled = true
        isVisible = true
        val column1 = ColumnConstraints()
        column1.prefWidth = 100.0
        column1.minWidth = 10.0
        column1.halignment = HPos.CENTER
        column1.hgrow = Priority.SOMETIMES
        val column2 = ColumnConstraints()
        column2.prefWidth = 100.0
        column2.minWidth = 10.0
        column2.halignment = HPos.CENTER
        column2.hgrow = Priority.SOMETIMES
        columnConstraints.clear()
        columnConstraints.addAll(column1, column2)
        val row = RowConstraints(10.0, 30.0, 30.0)
        row.vgrow = Priority.SOMETIMES
        rowConstraints.clear()
        rowConstraints.add(row)
        buttonPrevious = buildButton("←")
        add(buttonPrevious, 0, 0)
        disable(buttonPrevious)
        buttonNext = buildButton("→")
        add(buttonNext, 1, 0)
    }
}