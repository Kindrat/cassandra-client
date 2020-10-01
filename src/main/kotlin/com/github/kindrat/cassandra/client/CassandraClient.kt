package com.github.kindrat.cassandra.client

import com.github.kindrat.cassandra.client.ui.event.StageReadyEvent
import com.sun.javafx.application.ParametersImpl
import javafx.application.Platform
import javafx.stage.Stage
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext

class CassandraClient : FxApplication() {
    private lateinit var applicationContext: ConfigurableApplicationContext

    override fun start(primaryStage: Stage) {
        super.start(primaryStage)
        applicationContext.publishEvent(StageReadyEvent(primaryStage))
    }

    override fun init() {
        val parameters = ParametersImpl.getParameters(this)
        applicationContext = SpringApplicationBuilder(CassandraClientGUI::class.java)
                .run(*parameters.raw.toTypedArray())
    }

    override fun stop() {
        applicationContext.close()
        Platform.exit()
    }
}