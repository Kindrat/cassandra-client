package com.github.kindrat.cassandra.client.ui.listener

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import java.util.function.Consumer

class TextChangeListener(private val action: Consumer<String>) : ChangeListener<String> {

    override fun changed(observable: ObservableValue<out String>, oldValue: String, newValue: String) {
        if (oldValue != newValue) {
            action.accept(newValue)
        }
    }

}