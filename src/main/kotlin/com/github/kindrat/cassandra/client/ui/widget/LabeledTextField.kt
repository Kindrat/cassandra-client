package com.github.kindrat.cassandra.client.ui.widget

import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox

class LabeledTextField(label: String, default: String) : HBox() {
    private val label: Label = Label(label)
    private val textField: TextField = TextField(default)

    val dc: String
        get() = textField.text

    init {
        textField.onContextMenuRequested = EventHandler { }
        children.addAll(this.label, textField)
        width = 100.0
        spacing = 10.0
    }
}