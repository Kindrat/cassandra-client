package com.github.kindrat.cassandra.client.service

import com.github.kindrat.cassandra.client.service.tasks.BackgroundTask
import com.github.kindrat.cassandra.client.service.tasks.TaskStatistics
import java.util.*

class BackgroundTaskExecutor {
    private val runningTasks: MutableMap<UUID, BackgroundTask> = HashMap()

    fun submit(task: BackgroundTask): UUID {
        val taskId = UUID.randomUUID()
        runningTasks[taskId] = task
        task.run()
        return taskId
    }

    fun findTaskStatistics(taskId: UUID): TaskStatistics? {
        return runningTasks[taskId]?.statistics
    }

    fun stop(taskId: UUID) {
        runningTasks.remove(taskId)?.let {
            it.dispose()
            while (!it.isDisposed) {
                Thread.sleep(100)
            }
        }
    }
}