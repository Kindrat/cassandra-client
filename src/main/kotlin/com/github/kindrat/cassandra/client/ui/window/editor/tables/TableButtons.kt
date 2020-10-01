package com.github.kindrat.cassandra.client.ui.window.editor.tables

import com.github.kindrat.cassandra.client.util.UIUtil.buildButton
import com.github.kindrat.cassandra.client.util.UIUtil.disable
import com.github.kindrat.cassandra.client.util.UIUtil.enable
import com.github.kindrat.cassandra.client.util.UIUtil.fillParent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.control.Button
import javafx.scene.layout.*
import org.springframework.beans.factory.BeanFactory

internal class TableButtons : AnchorPane() {
    private val plusButton: Button
    private val commitButton: Button
    private val cancelButton: Button
    fun enableButtons() {
        enable(plusButton, commitButton, cancelButton)
    }

    fun disableButtons() {
        disable(plusButton, commitButton, cancelButton)
    }

    fun initActions(beanFactory: BeanFactory) {
        plusButton.onAction = EventHandler { beanFactory.getBean("tableEditor") }
    }

    private fun gridPane(): GridPane {
        val gridPane = GridPane()
        gridPane.columnConstraints.add(ColumnConstraints(35.0, 35.0, 35.0, Priority.SOMETIMES, HPos.CENTER, true))
        gridPane.columnConstraints.add(ColumnConstraints(35.0, 35.0, 35.0, Priority.SOMETIMES, HPos.CENTER, true))
        gridPane.columnConstraints.add(ColumnConstraints(10.0, 100.0, 100.0, Priority.SOMETIMES, HPos.CENTER, true))
        gridPane.columnConstraints.add(ColumnConstraints(35.0, 35.0, 35.0, Priority.SOMETIMES, HPos.CENTER, true))
        gridPane.columnConstraints.add(ColumnConstraints(35.0, 35.0, 35.0, Priority.SOMETIMES, HPos.CENTER, true))
        gridPane.columnConstraints.add(ColumnConstraints(10.0, 10.0, 10.0, Priority.SOMETIMES, HPos.CENTER, true))
        gridPane.rowConstraints.add(RowConstraints(30.0, 30.0, 30.0, Priority.SOMETIMES, VPos.CENTER, true))
        return gridPane
    }

    init {
        maxHeight = 40.0
        minHeight = 40.0
        prefHeight = 40.0
        maxWidth = 250.0
        minWidth = 250.0
        prefWidth = 250.0
        val gridPane = gridPane()
        children.add(gridPane)
        fillParent(gridPane)
        plusButton = buildButton("+")
        gridPane.add(plusButton, 0, 0)
        commitButton = buildButton("✓")
        gridPane.add(commitButton, 3, 0)
        cancelButton = buildButton("∅")
        gridPane.add(cancelButton, 4, 0)
    }
}