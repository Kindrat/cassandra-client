package com.github.kindrat.cassandra.client.service;

import com.github.kindrat.cassandra.client.service.tasks.BackgroundTask;
import com.github.kindrat.cassandra.client.service.tasks.TaskStatistics;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BackgroundTaskExecutor {
    private final Map<UUID, BackgroundTask> runningTasks = new HashMap<>();

    public UUID submit(BackgroundTask task) {
        UUID taskId = UUID.randomUUID();
        runningTasks.put(taskId, task);
        task.run();
        return taskId;
    }

    public Optional<TaskStatistics> findTaskStatistics(UUID taskId) {
        return Optional.ofNullable(runningTasks.get(taskId)).map(BackgroundTask::getStatistics);
    }

    @SneakyThrows
    public void stop(UUID taskId) {
        BackgroundTask task = runningTasks.remove(taskId);
        if (task != null) {
            task.dispose();
            while(!task.isDisposed()) {
                Thread.sleep(100);
            }
        }
    }
}
