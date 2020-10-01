package com.github.kindrat.cassandra.client

import javafx.application.Application
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration
import org.springframework.context.annotation.Lazy


fun main(vararg args: String) {
    Application.launch(CassandraClient::class.java, *args)
}

@Lazy
@SpringBootApplication(exclude = [
    CassandraAutoConfiguration::class,
    CassandraDataAutoConfiguration::class,
    CassandraReactiveDataAutoConfiguration::class
])
class CassandraClientGUI