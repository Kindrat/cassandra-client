package com.github.kindrat.cassandra.client.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "client.storage")
data class StorageProperties(
        val location: String = System.getProperty("java.io.tmpdir"),
        val propertiesFile: String = "connections.cfg"
)