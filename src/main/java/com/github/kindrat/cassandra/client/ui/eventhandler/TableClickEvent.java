package com.github.kindrat.cassandra.client.ui.eventhandler;

import com.github.kindrat.cassandra.client.ui.DataObject;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TableClickEvent implements EventHandler<MouseEvent> {
    private final TableView<DataObject> tableView;

    @Override
    public void handle(MouseEvent event) {
        //noinspection unchecked
        TablePosition<DataObject, ?> tablePosition = tableView.focusModelProperty()
                .get()
                .focusedCellProperty()
                .get();
        tableView.edit(tablePosition.getRow(), tablePosition.getTableColumn());
    }
}
