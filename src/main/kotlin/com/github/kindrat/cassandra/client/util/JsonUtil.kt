package com.github.kindrat.cassandra.client.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonUtil {
    private val mapper = jacksonObjectMapper()

    @JvmStatic
    fun toJson(pojo: Any?): String {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo)
    }

    @JvmStatic
    fun <T> fromJson(json: String?, type: Class<T>?): List<T> {
        val mapType = mapper.typeFactory.constructCollectionType(MutableList::class.java, type)
        return mapper.readerFor(mapType).readValue(json)
    }
}