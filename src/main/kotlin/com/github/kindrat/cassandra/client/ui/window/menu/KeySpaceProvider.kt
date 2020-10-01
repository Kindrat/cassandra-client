package com.github.kindrat.cassandra.client.ui.window.menu

import java.util.concurrent.CompletableFuture

@FunctionalInterface
fun interface KeySpaceProvider {
    fun loadKeyspaces(url: String, dc: String, username: String?, password: String?): CompletableFuture<List<String>>
}