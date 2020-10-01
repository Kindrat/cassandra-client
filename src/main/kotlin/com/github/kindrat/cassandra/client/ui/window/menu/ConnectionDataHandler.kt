package com.github.kindrat.cassandra.client.ui.window.menu

import com.github.kindrat.cassandra.client.ui.ConnectionData

@FunctionalInterface
fun interface ConnectionDataHandler {
    fun onConnectionData(connectionData: ConnectionData)
}