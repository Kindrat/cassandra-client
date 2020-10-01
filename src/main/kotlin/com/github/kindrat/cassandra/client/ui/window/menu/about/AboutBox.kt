package com.github.kindrat.cassandra.client.ui.window.menu.about

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.properties.UIProperties
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.stage.Modality
import javafx.stage.Stage

class AboutBox(parent: Stage, private val localeService: MessageByLocaleService, private val properties: UIProperties) : Stage() {
    private fun buildScene(): Scene {
        val aboutBox = VBox(properties.aboutBoxSpacing)
        val dialogScene = Scene(aboutBox, properties.aboutBoxWidth, properties.aboutBoxHeight)
        val text = Text(localeService.getMessage("ui.menu.help.about.text"))
        text.textAlignment = TextAlignment.CENTER
        text.wrappingWidth = properties.aboutBoxWidth
        aboutBox.children.add(text)
        return dialogScene
    }

    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parent)
        icons.add(Image("cassandra_ico.png"))
        title = localeService.getMessage("ui.menu.help.about.title")
        scene = buildScene()
        show()
    }
}