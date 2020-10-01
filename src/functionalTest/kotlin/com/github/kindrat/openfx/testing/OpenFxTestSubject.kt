package com.github.kindrat.openfx.testing

import com.github.kindrat.cassandra.client.FxApplication
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.retry.Repeat
import java.time.Duration

class OpenFxTestSubject<T : FxApplication>(private val application: T) : TestSubject<T> {
    override fun awaitVisible(timeout: Duration) {
        Mono.defer { Mono.just(application.primaryStage.isShowing) }
                .filter { it }
                .repeatWhenEmpty(Repeat.times<Boolean>(Long.MAX_VALUE).fixedBackoff(Duration.ofSeconds(2)))
                .subscribeOn(Schedulers.elastic())
                .block(timeout)
    }

    override fun findElement(id: String): Node? {
        return application.primaryStage.scene.lookup("#$id")
    }

    override fun visualBounds(): Rectangle2D {
        val primaryStage = application.primaryStage
        return Rectangle2D(primaryStage.x, primaryStage.y, primaryStage.width, primaryStage.height)
    }

    override fun stop() {
        application.stop()
    }
}