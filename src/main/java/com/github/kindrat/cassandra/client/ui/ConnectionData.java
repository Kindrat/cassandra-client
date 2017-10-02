package com.github.kindrat.cassandra.client.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionData {
    /**
     * cassandra url containing host and port (localhost:9042)
     */
    private String url;
    /**
     * cassandra keyspace
     */
    private String keyspace;
    /**
     * cassandra username
     */
    private String username;
    /**
     * cassandra password
     */
    private String password;
}
