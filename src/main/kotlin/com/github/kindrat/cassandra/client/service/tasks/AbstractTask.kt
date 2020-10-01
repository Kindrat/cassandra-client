package com.github.kindrat.cassandra.client.service.tasks

import com.github.kindrat.cassandra.client.service.tasks.TaskState.ACTIVE
import reactor.core.Disposable
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import java.lang.System.currentTimeMillis
import java.time.Duration
import java.util.*

abstract class AbstractTask : BackgroundTask {
    private val eventStream = EmitterProcessor.create<TaskStatistics>(false)

    override var statistics: TaskStatistics = TaskStatistics(ACTIVE, currentTimeMillis(), Duration.ZERO, 0.0)

    private var job: Disposable? = null
    override fun run(): Flux<TaskStatistics> {
        job = innerRun().subscribe({ eventStream.onNext(it) }) { eventStream.onError(it) }
        return eventStream
    }

    abstract fun innerRun(): Flux<TaskStatistics>

    override fun dispose() {
        job?.apply { this.dispose() }
        statistics = statistics.copy(state = TaskState.DISCARDED)
    }

    override fun isDisposed(): Boolean {
        return statistics.state !== ACTIVE
    }
}