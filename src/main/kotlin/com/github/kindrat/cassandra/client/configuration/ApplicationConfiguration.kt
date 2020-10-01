package com.github.kindrat.cassandra.client.configuration

import com.github.kindrat.cassandra.client.service.BackgroundTaskExecutor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource

@Configuration
class ApplicationConfiguration {
    @Bean
    fun messageSource(): ReloadableResourceBundleMessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename("classpath:i18n/messages")
        return messageSource
    }

    @Bean
    fun backgroundTaskExecutor(): BackgroundTaskExecutor {
        return BackgroundTaskExecutor()
    }
}