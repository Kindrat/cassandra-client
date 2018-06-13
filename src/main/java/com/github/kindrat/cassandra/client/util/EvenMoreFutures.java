package com.github.kindrat.cassandra.client.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

import static javafx.application.Platform.runLater;

@UtilityClass
public class EvenMoreFutures {
    public static <T> CompletableFuture<T> toCompletable(ListenableFuture<T> listenableFuture) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Futures.addCallback(listenableFuture, new FutureCallback<T>() {
            @Override
            public void onSuccess(@Nullable T result) {
                runLater(() -> future.complete(result));
            }

            @Override
            public void onFailure(@Nonnull Throwable t) {
                runLater(() -> future.completeExceptionally(t));
            }
        });
        return future;
    }
}
