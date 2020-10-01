package com.github.kindrat.cassandra.client.ui.fx.component

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox

class ToggleSwitch(on: String, off: String) : HBox() {

    private val label = Label()
    private val button = Button()
    private val switchedOn = SimpleBooleanProperty(false)

    fun switchOnProperty(): SimpleBooleanProperty {
        return switchedOn
    }

    fun onEnabled(action: Runnable?) {
        switchedOn.addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            if (newValue) {
                Platform.runLater(action)
            }
        }
    }

    fun onDisabled(action: Runnable?) {
        switchedOn.addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            if (!newValue) {
                Platform.runLater(action)
            }
        }
    }

    private fun setStyle() {
        //Default Width
        width = 100.0
        label.alignment = Pos.CENTER
        style = "-fx-background-color: lightgrey; -fx-text-fill:black; -fx-background-radius: 4;"
        alignment = Pos.CENTER_LEFT
    }

    private fun bindProperties() {
        label.prefWidthProperty().bind(widthProperty().divide(2))
        label.prefHeightProperty().bind(heightProperty())
        button.prefWidthProperty().bind(widthProperty().divide(2))
        button.prefHeightProperty().bind(heightProperty())
    }

    init {
        label.text = off
        children.addAll(label, button)
        button.onAction = EventHandler { switchedOn.set(!switchedOn.get()) }
        label.onMouseClicked = EventHandler { switchedOn.set(!switchedOn.get()) }
        setStyle()
        bindProperties()
        onEnabled(Runnable {
            label.text = on
            style = "-fx-background-color: lightgrey; -fx-text-fill:black; -fx-background-radius: 4;"
            label.toFront()
        })
        onDisabled(Runnable {
            label.text = off
            style = "-fx-background-color: lightgrey; -fx-text-fill:black; -fx-background-radius: 4;"
            button.toFront()
        })
    }
}