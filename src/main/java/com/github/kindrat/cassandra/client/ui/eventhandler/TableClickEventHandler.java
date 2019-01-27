package com.github.kindrat.cassandra.client.ui.eventhandler;

import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TableClickEventHandler<T> implements EventHandler<MouseEvent> {
    private final TableView<T> tableView;

    @Override
    public void handle(MouseEvent event) {
        //noinspection unchecked
        TablePosition<T, ?> tablePosition = tableView.focusModelProperty()
                .get()
                .focusedCellProperty()
                .get();
        tableView.edit(tablePosition.getRow(), tablePosition.getTableColumn());
    }
}
