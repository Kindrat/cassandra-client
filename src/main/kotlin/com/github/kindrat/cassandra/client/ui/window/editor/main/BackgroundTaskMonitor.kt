package com.github.kindrat.cassandra.client.ui.window.editor.main

import com.github.kindrat.cassandra.client.properties.UIProperties
import com.github.kindrat.cassandra.client.service.tasks.TaskStatistics
import com.github.kindrat.cassandra.client.util.UIUtil.setWidth
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class BackgroundTaskMonitor(properties: UIProperties) : Pane() {
    private val counter = AtomicInteger()
    private val taskStatistics: MutableMap<UUID, TaskStatistics?> = HashMap()
    private val label: Label

    fun addTask(id: UUID) {
        taskStatistics[id] = null
        label.text = "${counter.incrementAndGet()} ⏲"
    }

    fun update(id: UUID, statistics: TaskStatistics?) {
        taskStatistics[id] = statistics
    }

    fun remove(id: UUID?) {
        taskStatistics.remove(id)
        label.text = "${counter.decrementAndGet()} ⏲"
    }

    fun showTasks() {
        logger.debug("!!!!!!!!!!")
    }

    companion object {
        private const val TEMPLATE = "{} ⏲"
    }

    init {
        label = Label("${counter.get()} ⏲")
        label.alignmentProperty().value = Pos.BASELINE_CENTER
        setWidth(this, 20.0)
        children.add(label)
    }
}