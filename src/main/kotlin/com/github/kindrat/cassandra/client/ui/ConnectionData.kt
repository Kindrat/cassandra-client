package com.github.kindrat.cassandra.client.ui

import org.springframework.data.cassandra.config.CompressionType

data class ConnectionData(
        val url: String,
        val keyspace: String,
        val localDatacenter: String,
        val username: String?,
        val password: String?,
        val compressionType: CompressionType = CompressionType.LZ4
)