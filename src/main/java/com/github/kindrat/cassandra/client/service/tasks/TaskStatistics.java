package com.github.kindrat.cassandra.client.service.tasks;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;

/**
 * Various statistics task exposes to external services
 */
@Value
@Builder(toBuilder = true)
public class TaskStatistics {
    /**
     * Current state of task
     */
    private TaskState state;
    /**
     * Unix timestamp representing start time
     */
    private Long startTimestamp;
    /**
     * Total time spent on task execution
     */
    private Duration executionTime;
    /**
     * Percents of total work accomplished
     */
    private Double workDone;
}
