package com.github.kindrat.cassandra.client

import com.github.kindrat.cassandra.client.ui.window.menu.file.FileMenu
import com.github.kindrat.cassandra.client.util.menu
import com.github.kindrat.openfx.testing.FxExtensions.awaitShowing
import com.github.kindrat.openfx.testing.FxExtensions.click
import com.github.kindrat.openfx.testing.TestSubject
import com.github.kindrat.openfx.testing.junit.OpenFx
import com.github.kindrat.openfx.testing.junit.OpenFxTestExtension
import javafx.scene.control.MenuBar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(OpenFxTestExtension::class)
class MainWindowFT {

    @Test
    fun shouldOpenFileMenu(@OpenFx testApplication: TestSubject<CassandraClient>) {
        val topMenu = testApplication.findElement("menu") as MenuBar
        assertThat(topMenu).isNotNull
        assertThat(topMenu.menus).hasSize(2)

        val fileMenu = topMenu.menu(FileMenu.ID)
        assertThat(fileMenu).isNotNull
        assertThat(fileMenu?.isShowing).isFalse

        topMenu.click(fileMenu!!)
        fileMenu.awaitShowing()
        assertThat(fileMenu.isShowing).isTrue
    }
}