package com.github.kindrat.cassandra.client.ui.window.editor.main

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.service.tasks.TaskStatistics
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import java.util.*

class BackgroundTaskPopup(
        parent: Stage,
        taskStatistics: Map<UUID, TaskStatistics?>,
        localeService: MessageByLocaleService
) : Stage() {

    private fun buildScene(): Scene {
        TODO()
    }

    init {
        title = localeService.getMessage("ui.editor.export.title")
        icons.add(Image("cassandra_ico.png"))
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parent)
        scene = buildScene()
        minWidth = 200.0
        minHeight = 30.0
    }
}