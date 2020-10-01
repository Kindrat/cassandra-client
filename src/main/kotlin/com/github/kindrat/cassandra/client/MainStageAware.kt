package com.github.kindrat.cassandra.client

import javafx.application.Application
import javafx.stage.Stage

abstract class FxApplication : Application() {
    private lateinit var mainStage: Stage

    val primaryStage: Stage
        get() = mainStage

    override fun start(primaryStage: Stage) {
        mainStage = primaryStage
    }
}