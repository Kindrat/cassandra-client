package com.github.kindrat.cassandra.client.ui.event

import javafx.event.Event
import javafx.event.EventTarget
import javafx.event.EventType

class ErrorEvent constructor(source: Any?, target: EventTarget?, val error: Throwable) : Event(source, target, ERROR) {

    constructor(error: Throwable) : this(null, null, error)

    companion object {
        private const val serialVersionUID = -6171288362025898212L
        private val ERROR = EventType<ErrorEvent>(ANY, "ERROR")
    }
}