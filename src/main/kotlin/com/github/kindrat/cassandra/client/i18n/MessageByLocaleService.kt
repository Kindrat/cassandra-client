package com.github.kindrat.cassandra.client.i18n

interface MessageByLocaleService {
    fun getMessage(id: String): String
}