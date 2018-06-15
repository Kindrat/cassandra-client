package com.github.kindrat.cassandra.client.ui.eventhandler;

import com.github.kindrat.cassandra.client.filter.DataFilter;
import com.github.kindrat.cassandra.client.ui.DataObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FilterBtnHandler implements EventHandler<ActionEvent> {
    private final TextField filterTb;
    private final TableView<DataObject> dataTable;
    private final ObservableList<DataObject> originalData;

    @Override
    public void handle(ActionEvent event) {
        if (filterTb.getText().isEmpty()) {
            if (dataTable.getItems().size() != originalData.size()) {
                dataTable.setItems(originalData);
            }
        } else {
            Predicate<DataObject> rowPredicate = DataFilter.parsePredicate(filterTb.getText());
            List<DataObject> filtered = originalData.stream().filter(rowPredicate).collect(Collectors.toList());
            dataTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }
}
