package com.github.kindrat.openfx.testing

import com.github.kindrat.cassandra.client.FxApplication
import javafx.application.Application
import javafx.application.Preloader
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.stage.Stage
import mu.KotlinLogging
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoProcessor
import reactor.core.scheduler.Schedulers
import java.time.Duration

private val logger = KotlinLogging.logger {}

interface TestSubject<T : FxApplication> {
    fun awaitVisible() = awaitVisible(Duration.ofSeconds(30))

    fun awaitVisible(timeout: Duration)

    fun findElement(id: String): Node?

    fun visualBounds(): Rectangle2D

    fun stop()

    companion object {
        private val processor = MonoProcessor.create<FxApplication>()
        fun <T : FxApplication> create(root: Class<T>, timeout: Duration = Duration.ofSeconds(20),
                                     vararg args: String): TestSubject<T> {
            System.setProperty("javafx.preloader", TestPreloader::class.java.name)
            return Mono.defer { processor }
                    .cast(root)
                    .map { OpenFxTestSubject(it) }
                    .doOnError { logger.error("Failed to start application", it) }
                    .doOnSubscribe {
                        logger.info { "Waiting for app to start" }
                        Mono.fromRunnable<Any> { Application.launch(root, *args) }
                                .subscribeOn(Schedulers.elastic())
                                .subscribe()
                    }
                    .subscribeOn(Schedulers.elastic())
                    .block(timeout)!!
        }
    }

    class TestPreloader : Preloader() {
        override fun handleStateChangeNotification(notification: StateChangeNotification?) {
            if (notification?.type == StateChangeNotification.Type.BEFORE_START) {
                val application = notification.application as FxApplication
                processor.onNext(application)
                processor.onComplete()
            }
        }

        override fun start(primaryStage: Stage?) {
        }
    }
}