package com.github.kindrat.cassandra.client.service

import com.github.kindrat.cassandra.client.ui.MainController
import com.github.kindrat.cassandra.client.ui.event.StageInitializedEvent
import com.github.kindrat.cassandra.client.ui.event.StageReadyEvent
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component

@Component
@ConstructorBinding
class StageInitializer(
        private val mainView: Parent, private val applicationContext: ConfigurableApplicationContext
): ApplicationListener<StageReadyEvent> {

    override fun onApplicationEvent(event: StageReadyEvent) {
        val stage = event.stage
        stage.title = "Cassandra client"
        stage.icons.add(Image("cassandra_ico.png"))
        stage.scene = Scene(mainView)
        stage.isResizable = true
        stage.centerOnScreen()
        stage.show()
        applicationContext.publishEvent(StageInitializedEvent(stage))
    }
}