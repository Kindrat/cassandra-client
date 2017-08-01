package com.github.kindrat.cassandra.client.ui.eventhandler;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import lombok.RequiredArgsConstructor;

/**
 * Event handler for text fields with buttons performing some actions on field content. Will disable
 */
@RequiredArgsConstructor
public class TextFieldButtonWatcher implements ChangeListener<String> {
    private final Button button;

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        button.setDisable(newValue.isEmpty());
    }

    public static TextFieldButtonWatcher wrap(Button button) {
        return new TextFieldButtonWatcher(button);
    }
}
