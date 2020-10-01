package com.github.kindrat.cassandra.client.ui.listener

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

class CheckBoxListener private constructor(
        private val onEnabled: Runnable?,
        private val onDisabled: Runnable?
) : ChangeListener<Boolean> {

    override fun changed(observable: ObservableValue<out Boolean>, oldValue: Boolean, newValue: Boolean) {
        if (oldValue != newValue) {
            if (newValue) {
                onEnabled?.run()
            } else {
                onDisabled?.run()
            }
        }
    }

    companion object {
        fun createForEnable(onEnabled: Runnable?): CheckBoxListener {
            return CheckBoxListener(onEnabled, null)
        }

        fun createForDisable(onDisabled: Runnable?): CheckBoxListener {
            return CheckBoxListener(null, onDisabled)
        }

        @JvmStatic
        fun create(onEnabled: Runnable?, onDisabled: Runnable?): CheckBoxListener {
            return CheckBoxListener(onEnabled, onDisabled)
        }
    }

}