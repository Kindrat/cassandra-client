package com.github.kindrat.cassandra.client.ui.window.editor.main;

import com.github.kindrat.cassandra.client.properties.UIProperties;
import com.github.kindrat.cassandra.client.service.tasks.TaskStatistics;
import com.github.kindrat.cassandra.client.util.UIUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.nginate.commons.lang.NStrings.format;

@Slf4j
public class BackgroundTaskMonitor extends Pane {
    private static final String TEMPLATE = "{} ⏲";
    private final AtomicInteger counter = new AtomicInteger();
    private final Map<UUID, TaskStatistics> taskStatistics = new HashMap<>();

    private final Label label;

    public BackgroundTaskMonitor(UIProperties properties) {
        label = new Label(format(TEMPLATE, counter.get()));
        label.alignmentProperty().setValue(Pos.BASELINE_CENTER);
        UIUtil.setWidth(this, 20);
        getChildren().add(label);
    }

    public void addTask(UUID id) {
        taskStatistics.put(id, null);
        label.setText(format(TEMPLATE, counter.incrementAndGet()));
    }

    public void update(UUID id, TaskStatistics statistics) {
        taskStatistics.put(id, statistics);
    }

    public void remove(UUID id) {
        taskStatistics.remove(id);
        label.setText(format(TEMPLATE, counter.decrementAndGet()));
    }

    public void showTasks() {
        log.debug("!!!!!!!!!!");
    }
}
