package com.github.kindrat.cassandra.client.ui.menu;

import com.github.kindrat.cassandra.client.ui.ConnectionData;

@FunctionalInterface
public interface ConnectionDataHandler {
    void onConnectionData(ConnectionData connectionData);
}
