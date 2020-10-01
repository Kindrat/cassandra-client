package com.github.kindrat.cassandra.client.ui.fx.component

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class LabeledComponentColumn(spacing: Double) : VBox(spacing) {

    fun addLabeledElement(label: String, element: Node) {
        val row = HBox(spacing)
        val labelColumn = VBox(spacing)
        labelColumn.alignment = Pos.CENTER_LEFT
        labelColumn.prefWidthProperty().bind(widthProperty().multiply(0.6))
        labelColumn.children.add(Label(label))
        val elementColumn = VBox(spacing)
        elementColumn.alignment = Pos.CENTER_LEFT
        elementColumn.prefWidthProperty().bind(widthProperty().multiply(0.25))
        elementColumn.children.add(element)
        row.children.addAll(labelColumn, elementColumn)
        children.add(row)
    }
}