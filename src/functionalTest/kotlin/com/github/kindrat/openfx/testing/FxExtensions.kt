package com.github.kindrat.openfx.testing

import com.sun.javafx.scene.control.MenuBarButton
import javafx.application.Platform
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.skin.MenuBarSkin
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.robot.Robot
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.retry.Repeat
import java.time.Duration

object FxExtensions {
    fun MenuBar.click(menu: Menu) {
        val menuBarSkin = skin as MenuBarSkin
        val hBox: HBox = menuBarSkin.children[0] as HBox
        val menuBarButton = hBox.children.find { it.id == menu.id } as MenuBarButton
        val screenCoordinates = menuBarButton.localToScreen(menuBarButton.boundsInLocal)
        Platform.runLater {
            val robot = Robot()
            robot.mouseMove(screenCoordinates.centerX, screenCoordinates.centerY)
            robot.mouseClick(MouseButton.PRIMARY)
        }
    }

    fun Menu.awaitShowing(timeout: Duration = Duration.ofSeconds(10)) {
        Mono.fromCallable { isShowing }
                .filter { it }
                .repeatWhenEmpty(Repeat.times<Boolean>(Long.MAX_VALUE))
                .then(Mono.delay(Duration.ofSeconds(1)))
                .timeout(timeout)
                .subscribeOn(Schedulers.elastic())
                .block()
    }
}