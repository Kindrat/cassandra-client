package com.github.kindrat.cassandra.client.ui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import static javafx.application.Platform.runLater;

public class ToggleSwitch extends HBox {
    private final Label label = new Label();
    private final Button button = new Button();

    private SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(false);

    public ToggleSwitch(String on, String off) {
        label.setText(off);
        getChildren().addAll(label, button);
        button.setOnAction((e) -> switchedOn.set(!switchedOn.get()));
        label.setOnMouseClicked((e) -> switchedOn.set(!switchedOn.get()));

        setStyle();
        bindProperties();

        onEnabled(() -> {
            label.setText(on);
            setStyle("-fx-background-color: lightgrey; -fx-text-fill:black; -fx-background-radius: 4;");
            label.toFront();
        });
        onDisabled(() -> {
            label.setText(off);
            setStyle("-fx-background-color: lightgrey; -fx-text-fill:black; -fx-background-radius: 4;");
            button.toFront();
        });
    }

    public SimpleBooleanProperty switchOnProperty() {
        return switchedOn;
    }

    public void onEnabled(Runnable action) {
        switchedOn.addListener((a, oldValue, newValue) -> {
            if (newValue) {
                runLater(action);
            }
        });
    }

    public void onDisabled(Runnable action) {
        switchedOn.addListener((a, oldValue, newValue) -> {
            if (!newValue) {
                runLater(action);
            }
        });
    }

    private void setStyle() {
        //Default Width
        setWidth(100);
        label.setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: lightgrey; -fx-text-fill:black; -fx-background-radius: 4;");
        setAlignment(Pos.CENTER_LEFT);
    }

    private void bindProperties() {
        label.prefWidthProperty().bind(widthProperty().divide(2));
        label.prefHeightProperty().bind(heightProperty());
        button.prefWidthProperty().bind(widthProperty().divide(2));
        button.prefHeightProperty().bind(heightProperty());
    }
}
