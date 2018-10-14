package com.github.kindrat.cassandra.client.ui.window.menu;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface KeySpaceProvider {
    CompletableFuture<List<String>> loadKeyspaces(String url, String username, String password);
}
