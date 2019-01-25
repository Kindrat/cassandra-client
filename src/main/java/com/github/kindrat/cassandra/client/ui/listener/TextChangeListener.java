package com.github.kindrat.cassandra.client.ui.listener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class TextChangeListener implements ChangeListener<String> {
    private final Consumer<String> action;

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            action.accept(newValue);
        }
    }
}
