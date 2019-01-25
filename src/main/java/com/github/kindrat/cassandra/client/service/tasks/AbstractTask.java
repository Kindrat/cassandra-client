package com.github.kindrat.cassandra.client.service.tasks;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Slf4j
public abstract class AbstractTask implements BackgroundTask {
    private final EmitterProcessor<TaskStatistics> eventStream = EmitterProcessor.create(false);
    private TaskStatistics lastState;
    private Disposable job;

    @Override
    public TaskStatistics getStatistics() {
        return lastState;
    }

    @Override
    public Flux<TaskStatistics> run() {
        job = innerRun().subscribe(eventStream::onNext, eventStream::onError);
        return eventStream;
    }

    abstract Flux<TaskStatistics> innerRun();

    @Override
    public void dispose() {
        Optional.ofNullable(job).ifPresent(Disposable::dispose);
        lastState = lastState.toBuilder()
                .state(TaskState.DISCARDED)
                .build();
    }

    @Override
    public boolean isDisposed() {
        return lastState.getState() != TaskState.ACTIVE;
    }
}
