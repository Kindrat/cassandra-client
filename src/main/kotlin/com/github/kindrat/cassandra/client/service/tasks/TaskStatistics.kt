package com.github.kindrat.cassandra.client.service.tasks

import java.time.Duration

/**
 * Various statistics task exposes to external services
 */
data class TaskStatistics(val state: TaskState, val startTimestamp: Long,
                          val executionTime: Duration, val workDone: Double)