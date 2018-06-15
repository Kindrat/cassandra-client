package com.github.kindrat.cassandra.client.ui.window.editor.main.filter;

import com.datastax.driver.core.TableMetadata;
import com.github.kindrat.cassandra.client.ui.DataObject;
import com.github.kindrat.cassandra.client.ui.eventhandler.FilterBtnHandler;
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.DataTableView;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class FilterGrid extends GridPane {
    private final Button filterButton;
    private final FilterTextField textField;
    private final DataTableView dataTableView;

    public FilterGrid(DataTableView dataTableView, FilterTextField textField) {
        this.dataTableView = dataTableView;
        this.filterButton = new Button("Filter");
        this.filterButton.setMnemonicParsing(false);
        this.textField = textField;

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPrefWidth(100);
        column1.setMinWidth(10);
        column1.setHalignment(HPos.CENTER);
        column1.setHgrow(Priority.SOMETIMES);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPrefWidth(100);
        column2.setMinWidth(100);
        column2.setMaxWidth(100);
        column2.setHalignment(HPos.CENTER);
        column2.setHgrow(Priority.SOMETIMES);

        getColumnConstraints().clear();
        getColumnConstraints().addAll(column1, column2);

        RowConstraints row = new RowConstraints(10, 30, 30);
        row.setVgrow(Priority.SOMETIMES);

        getRowConstraints().clear();
        getRowConstraints().add(row);

        add(textField, 0, 0);
        add(filterButton, 1, 0);
    }

    public void setTableMetadata(TableMetadata tableMetadata) {
        textField.setTableMetadata(tableMetadata);
    }

    public void suggestCompletion() {
        if (textField.isFocused()) {
            textField.suggestCompletion();
        }
    }

    public void onDataUpdated(ObservableList<DataObject> data) {
        filterButton.setOnAction(new FilterBtnHandler(textField, dataTableView, data));
        textField.setOnAction(new FilterBtnHandler(textField, dataTableView, data));
    }
}
