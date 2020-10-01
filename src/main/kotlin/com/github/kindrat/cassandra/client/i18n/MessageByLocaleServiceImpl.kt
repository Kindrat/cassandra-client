package com.github.kindrat.cassandra.client.i18n

import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service

@Service
@ConstructorBinding
class MessageByLocaleServiceImpl(private val messageSource: MessageSource) : MessageByLocaleService {

    override fun getMessage(id: String): String {
        val locale = LocaleContextHolder.getLocale()
        return messageSource.getMessage(id, null, locale)
    }
}