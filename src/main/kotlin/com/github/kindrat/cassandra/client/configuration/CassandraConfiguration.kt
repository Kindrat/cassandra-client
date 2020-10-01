package com.github.kindrat.cassandra.client.configuration

import com.datastax.oss.driver.api.core.CqlSessionBuilder
import com.datastax.oss.driver.api.core.config.DefaultDriverOption
import com.datastax.oss.driver.api.core.config.DriverOption
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultProgrammaticDriverConfigLoaderBuilder
import com.github.kindrat.cassandra.client.exception.UrlSyntaxException
import com.github.kindrat.cassandra.client.ui.ConnectionData
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.data.cassandra.config.CqlSessionFactoryBean
import org.springframework.data.cassandra.config.SessionBuilderConfigurer
import java.util.*

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@EnableConfigurationProperties(CassandraProperties::class)
class CassandraConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun cassandraSession(connectionData: ConnectionData): CqlSessionFactoryBean {
        if (StringUtils.isAnyBlank(connectionData.keyspace, connectionData.url)) {
            throw UrlSyntaxException("Both url and keyspace are required")
        }
        val urlParts = connectionData.url.split(":")
        if (urlParts.size != 2) {
            throw UrlSyntaxException("Url should contain host:port")
        }
        val port: Int
        port = try {
            urlParts[1].toInt()
        } catch (e: Exception) {
            throw UrlSyntaxException("Invalid port : " + urlParts[1])
        }
        val bean = CqlSessionFactoryBean()
        bean.setContactPoints(urlParts[0])
        bean.setKeyspaceName(connectionData.keyspace)
        bean.setLocalDatacenter(connectionData.localDatacenter)
        bean.setPort(port)
        bean.setSessionBuilderConfigurer(getSessionBuilderConfigurerWrapper(connectionData))
        return bean
    }

    private fun getSessionBuilderConfigurerWrapper(connectionData: ConnectionData): SessionBuilderConfigurer {
        return SessionBuilderConfigurer { sessionBuilder: CqlSessionBuilder ->
            val builder: ProgrammaticDriverConfigLoaderBuilder = DefaultProgrammaticDriverConfigLoaderBuilder({
                val options = CassandraDriverOptions()
                        .add(DefaultDriverOption.SESSION_NAME, connectionData.url + "-" + connectionData.keyspace)
                        .add(DefaultDriverOption.PROTOCOL_COMPRESSION, connectionData.compressionType)
                ConfigFactory.invalidateCaches()
                ConfigFactory.defaultOverrides()
                        .withFallback(options.build())
                        .withFallback(ConfigFactory.defaultReference())
                        .resolve()
            }, DefaultDriverConfigLoader.DEFAULT_ROOT_PATH)
            sessionBuilder.withConfigLoader(builder.build())
            if (!connectionData.username.isNullOrBlank() && !connectionData.password.isNullOrBlank()) {
                sessionBuilder.withAuthCredentials(connectionData.username, connectionData.password)
            }
            sessionBuilder
        }
    }

    private class CassandraDriverOptions {
        private val options: MutableMap<String, String> = LinkedHashMap()
        fun add(option: DriverOption, value: String): CassandraDriverOptions {
            val key = createKeyFor(option)
            options[key] = value
            return this
        }

        fun add(option: DriverOption, value: Enum<*>): CassandraDriverOptions {
            return add(option, value.name)
        }

        fun build(): Config {
            return ConfigFactory.parseMap(options, "Environment")
        }

        companion object {
            private fun createKeyFor(option: DriverOption): String {
                return "${DefaultDriverConfigLoader.DEFAULT_ROOT_PATH}.${option.path}"
            }
        }
    }
}