package com.github.kindrat.cassandra.client.ui.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import lombok.Getter;

public class ErrorEvent extends Event {
    private static final long serialVersionUID = -6171288362025898212L;
    private static final EventType<ErrorEvent> ERROR = new EventType<>(Event.ANY, "ERROR");

    @Getter
    private final Throwable error;

    public ErrorEvent(Throwable error) {
        super(ERROR);
        this.error = error;
    }

    public ErrorEvent(Object source, EventTarget target, Throwable error) {
        super(source, target, ERROR);
        this.error = error;
    }
}
