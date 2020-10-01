package com.github.kindrat.cassandra.client.ui.listener

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.Button

/**
 * Event handler for text fields with buttons performing some actions on field content. Will disable
 */
class TextFieldButtonWatcher(private val button: Button) : ChangeListener<String> {
    override fun changed(observable: ObservableValue<out String>, oldValue: String, newValue: String) {
        button.isDisable = newValue.isEmpty()
    }

    companion object {
        @JvmStatic
        fun wrap(button: Button): TextFieldButtonWatcher {
            return TextFieldButtonWatcher(button)
        }
    }

}