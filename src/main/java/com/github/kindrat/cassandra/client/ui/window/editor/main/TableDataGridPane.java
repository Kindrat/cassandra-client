package com.github.kindrat.cassandra.client.ui.window.editor.main;

import com.datastax.driver.core.TableMetadata;
import com.github.kindrat.cassandra.client.service.TableContext;
import com.github.kindrat.cassandra.client.ui.DataObject;
import com.github.kindrat.cassandra.client.ui.keylistener.TableCellCopyHandler;
import com.github.kindrat.cassandra.client.ui.window.editor.main.filter.FilterGrid;
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.DataTableView;
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.PaginationPanel;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.List;

import static com.github.kindrat.cassandra.client.util.UIUtil.fillParent;
import static javafx.application.Platform.runLater;

public class TableDataGridPane extends GridPane {
    private final EmitterProcessor<DataObject> processor = EmitterProcessor.create();
    private final FluxSink<DataObject> sink = processor.sink();
    private final FilterGrid filterGrid;
    private final DataTableView dataTableView;
    private final PaginationPanel paginationPanel;

    public TableDataGridPane(FilterGrid filterGrid, DataTableView dataTableView, PaginationPanel paginationPanel) {
        this.filterGrid = filterGrid;
        this.dataTableView = dataTableView;
        this.paginationPanel = paginationPanel;
        setLayoutX(78);
        setLayoutY(131);
        setVisible(false);

        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(Priority.SOMETIMES);
        column.setMinWidth(10);
        column.setPrefWidth(100);

        getColumnConstraints().clear();
        getColumnConstraints().add(column);

        RowConstraints row1 = new RowConstraints();
        row1.setMaxHeight(40);
        row1.setMinHeight(40);
        row1.setPrefHeight(40);
        row1.setVgrow(Priority.SOMETIMES);

        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.SOMETIMES);

        RowConstraints row3 = new RowConstraints();
        row3.setMaxHeight(40);
        row3.setMinHeight(40);
        row3.setPrefHeight(40);
        row3.setVgrow(Priority.SOMETIMES);

        getRowConstraints().clear();
        getRowConstraints().addAll(row1, row2, row3);

        add(filterGrid, 0, 0);
        add(dataTableView, 0, 1);
        add(paginationPanel, 0, 2);

        fillParent(this);
    }

    public void updateTableMetadata(TableMetadata metadata) {
        dataTableView.onTableSelected(metadata);
        filterGrid.setTableMetadata(metadata);
    }

    public void setDataColumns(List<TableColumn<DataObject, Object>> columns) {
        dataTableView.setDataColumns(columns);
    }

    public void setData(TableContext context, ObservableList<DataObject> data) {
        dataTableView.setItems(data);
        dataTableView.refresh();
        filterGrid.onDataUpdated(data);

        dataTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                TableView.TableViewSelectionModel<DataObject> selectionModel = dataTableView.getSelectionModel();
                DataObject selectedItem = selectionModel.getSelectedItem();
                int selectedIndex = selectionModel.getSelectedIndex();

                data.remove(selectedIndex);
                sink.next(selectedItem);
            }
        });

        paginationPanel.applyOnTable(context, dataObjects -> runLater(() -> {
            dataTableView.setItems(dataObjects);
            dataTableView.refresh();
        }));

        setVisible(true);
    }

    public Flux<DataObject> objectsToRemoveStream() {
        return processor;
    }

    public TableCellCopyHandler buildCopyHandler() {
        return dataTableView.buildCopyHandler();
    }

    public void suggestCompletion() {
        filterGrid.suggestCompletion();
    }
}
