package com.github.kindrat.cassandra.client.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static javafx.application.Platform.runLater;

@Slf4j
@UtilityClass
public class EvenMoreFutures {
    public static <T> CompletableFuture<T> toCompletable(ListenableFuture<T> listenableFuture) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Futures.addCallback(listenableFuture, new FutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                runLater(() -> future.complete(result));
            }

            @Override
            public void onFailure(Throwable t) {
                runLater(() -> future.completeExceptionally(t));
            }
        });
        return future;
    }

    public static <O, T extends Throwable> BiConsumer<? super O, ? super T> logErrorIfPresent(String message) {
        return (obj, throwable) -> {
            if (throwable != null) {
                log.error(message, throwable);
            }
        };
    }

    public static <O, T extends Throwable> BiConsumer<? super O, ? super T> loggingConsumer(Function<O, ?> extractor) {
        return (obj, throwable) -> {
            if (throwable != null) {
                log.error(throwable.getMessage(), throwable);
            } else {
                log.info("Result : {}", extractor.apply(obj));
            }
        };
    }

    public static <O, T extends Throwable> BiConsumer<? super O, ? super T> handleErrorIfPresent(Consumer<T> handler) {
        return (obj, throwable) -> {
            if (throwable != null) {
                handler.accept(throwable);
            }
        };
    }
}
