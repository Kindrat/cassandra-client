package com.github.kindrat.cassandra.client.ui;

import javafx.scene.Parent;
import javafx.stage.Stage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class View {
    private final Parent view;
    private final Object controller;
    private Stage primaryStage;
}
