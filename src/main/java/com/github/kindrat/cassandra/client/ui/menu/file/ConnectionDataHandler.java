package com.github.kindrat.cassandra.client.ui.menu.file;

@FunctionalInterface
public interface ConnectionDataHandler {
    void onConnectionData(String url, String keyspace, String username, String password);
}
