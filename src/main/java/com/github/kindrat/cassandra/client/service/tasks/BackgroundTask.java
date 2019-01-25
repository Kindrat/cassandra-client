package com.github.kindrat.cassandra.client.service.tasks;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

/**
 * Describes generic rules for all background tasks
 */
public interface BackgroundTask extends Disposable {
    /**
     * Get task statistics. Statistics object is immutable
     *
     * @return task statistics
     */
    TaskStatistics getStatistics();

    Flux<TaskStatistics> run();
}
