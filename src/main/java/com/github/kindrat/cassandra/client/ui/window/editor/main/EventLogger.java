package com.github.kindrat.cassandra.client.ui.window.editor.main;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import lombok.extern.slf4j.Slf4j;

import static com.github.nginate.commons.lang.NStrings.format;
import static javafx.application.Platform.runLater;

@Slf4j
public class EventLogger extends GridPane {
    private final Label eventLabel;
    private final Label serverLabel;

    public EventLogger() {
        setPrefHeight(30);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(65);
        column1.setHgrow(Priority.SOMETIMES);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(20);
        column2.setHgrow(Priority.SOMETIMES);

        getColumnConstraints().clear();
        getColumnConstraints().addAll(column1, column2);

        RowConstraints row = new RowConstraints(10, 40, 40);
        row.setVgrow(Priority.SOMETIMES);

        getRowConstraints().clear();
        getRowConstraints().add(row);

        eventLabel = buildEventLabel();
        setConstraints(eventLabel, 0, 0);
        GridPane.setMargin(eventLabel, new Insets(0, 10, 0, 10));

        serverLabel = new Label();
        setConstraints(serverLabel, 1, 0);
        GridPane.setMargin(serverLabel, new Insets(0, 10, 0, 10));
        getChildren().addAll(eventLabel, serverLabel);
    }

    public void fireLogEvent(String template, Object... args) {
        runLater(() -> {
            String message = format(template, args);
            log.info(message);
            eventLabel.setText(message);
        });
    }

    public void clear() {
        runLater(() -> {
            eventLabel.setText("");
            serverLabel.setText("");
        });
    }

    public void printServerName(String url, String keyspace) {
        runLater(() -> serverLabel.setText(url + "/" + keyspace));
    }

    private Label buildEventLabel() {
        Label label = new Label();
        //to create new
        label.opaqueInsetsProperty();
        return label;
    }
}
