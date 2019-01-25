package com.github.kindrat.cassandra.client.ui.listener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckBoxListener implements ChangeListener<Boolean> {
    private final Runnable onEnabled;
    private final Runnable onDisabled;

    public static CheckBoxListener createForEnable(Runnable onEnabled) {
        return new CheckBoxListener(onEnabled, null);
    }

    public static CheckBoxListener createForDisable(Runnable onDisabled) {
        return new CheckBoxListener(null, onDisabled);
    }

    public static CheckBoxListener create(Runnable onEnabled, Runnable onDisabled) {
        return new CheckBoxListener(onEnabled, onDisabled);
    }


    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            if (newValue) {
                Optional.ofNullable(onEnabled).ifPresent(Runnable::run);
            } else {
                Optional.ofNullable(onDisabled).ifPresent(Runnable::run);
            }
        }
    }
}
