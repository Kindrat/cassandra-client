package com.github.kindrat.cassandra.client.ui.window.menu;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface KeySpaceProvider {
    CompletableFuture<List<String>> loadKeyspaces(String url, @Nullable String username, @Nullable String password);
}
