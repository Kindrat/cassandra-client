package com.github.kindrat.cassandra.client.ui.window.editor.main

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class EventLogger : GridPane() {
    private val eventLabel: Label
    private val serverLabel: Label

    fun fireLogEvent(message: String) {
        Platform.runLater {
            logger.info(message)
            eventLabel.text = message
        }
    }

    fun clear() {
        Platform.runLater {
            eventLabel.text = ""
            serverLabel.text = ""
        }
    }

    fun printServerName(url: String, keyspace: String) {
        Platform.runLater { serverLabel.text = "$url/$keyspace" }
    }

    private fun buildEventLabel(): Label {
        val label = Label()
        //to create new
        label.opaqueInsetsProperty()
        return label
    }

    init {
        prefHeight = 30.0
        val column1 = ColumnConstraints()
        column1.percentWidth = 65.0
        column1.hgrow = Priority.SOMETIMES
        val column2 = ColumnConstraints()
        column2.percentWidth = 20.0
        column2.hgrow = Priority.SOMETIMES
        columnConstraints.clear()
        columnConstraints.addAll(column1, column2)
        val row = RowConstraints(10.0, 40.0, 40.0)
        row.vgrow = Priority.SOMETIMES
        rowConstraints.clear()
        rowConstraints.add(row)
        eventLabel = buildEventLabel()
        setConstraints(eventLabel, 0, 0)
        setMargin(eventLabel, Insets(0.0, 10.0, 0.0, 10.0))
        serverLabel = Label()
        setConstraints(serverLabel, 1, 0)
        setMargin(serverLabel, Insets(0.0, 10.0, 0.0, 10.0))
        children.addAll(eventLabel, serverLabel)
    }
}