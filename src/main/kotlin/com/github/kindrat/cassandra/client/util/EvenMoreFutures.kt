package com.github.kindrat.cassandra.client.util

import mu.KotlinLogging
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function

private val logger = KotlinLogging.logger {}

object EvenMoreFutures {
    @JvmStatic
    fun <O, T : Throwable?> logErrorIfPresent(message: String?): BiConsumer<in O, in T> {
        return BiConsumer { _: O, throwable: T? -> throwable?.let { logger.error(message, it)  } }
    }

    @JvmStatic
    fun <O, T : Throwable?> loggingConsumer(extractor: Function<O, *>): BiConsumer<in O, in T> {
        return BiConsumer { obj: O, throwable: T? ->
            if (throwable != null) {
                logger.error(throwable.message, throwable)
            } else {
                logger.info("Result : ${extractor.apply(obj)}")
            }
        }
    }

    @JvmStatic
    fun <O, T : Throwable?> handleErrorIfPresent(lambda: (T) -> Unit): BiConsumer<in O, in T> {
        return BiConsumer { _: O, throwable: T? -> throwable?.let { lambda.invoke(it) } }
    }
}