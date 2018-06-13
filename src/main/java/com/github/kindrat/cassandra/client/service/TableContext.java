package com.github.kindrat.cassandra.client.service;

import com.datastax.driver.core.*;
import com.github.kindrat.cassandra.client.ui.DataObject;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.kindrat.cassandra.client.util.EvenMoreFutures.toCompletable;
import static com.github.kindrat.cassandra.client.util.UIUtil.cellFactory;

@Slf4j
public class TableContext {

    private final Map<Integer, PagingState> pagingStates = new HashMap<>();
    @Getter
    private final String table;
    private final String query;
    @Getter
    private final TableMetadata tableMetadata;
    @Getter
    private final List<TableColumn<DataObject, Object>> columns;
    private final CassandraClientAdapter clientAdapter;
    private final int pageSize;

    private int page;
    private CompletableFuture<ObservableList<DataObject>> currentPage;
    private volatile ResultSet resultSet;
    private volatile boolean isFinalPage;

    private TableContext(String table, String query, TableMetadata tableMetadata, CassandraClientAdapter clientAdapter,
            int pageSize) {
        this.table = table;
        this.tableMetadata = tableMetadata;
        this.clientAdapter = clientAdapter;
        this.pageSize = pageSize;
        this.query = query;
        columns = buildColumns();
    }

    public static TableContext fullTable(String table, TableMetadata tableMetadata,
            CassandraClientAdapter clientAdapter, int pageSize) {
        return new TableContext(table, "select * from " + table, tableMetadata, clientAdapter, pageSize);
    }

    public static TableContext customView(String table, String query, TableMetadata tableMetadata,
            CassandraClientAdapter clientAdapter, int pageSize) {
        return new TableContext(table, query, tableMetadata, clientAdapter, pageSize);
    }

    @Synchronized
    public CompletableFuture<ObservableList<DataObject>> nextPage() {
        if (isFinalPage) {
            return currentPage;
        }
        page++;
        currentPage = toCompletable(resultSet
                .fetchMoreResults())
                .thenApply(this::parseResultSet)
                .thenApply(FXCollections::observableList);
        return currentPage;
    }

    @Synchronized
    public CompletableFuture<ObservableList<DataObject>> previousPage() {
        page = Math.max(0, page - 1);
        currentPage = loadPage(page);
        return currentPage;
    }

    @Synchronized
    private CompletableFuture<ObservableList<DataObject>> loadPage(int page) {
        Statement statement = getStatement();
        PagingState pagingState = pagingStates.get(page);
        if (pagingState != null) {
            log.info("Using request paging {}", pagingState);
            statement.setPagingState(pagingState);
        }
        return clientAdapter.executeStatement(statement)
                .whenComplete((resultSet, throwable) -> {
                    if (throwable != null) {
                        log.error("Page {} load failed", page, throwable.getMessage());
                    } else {
                        PagingState nextPageState = resultSet.getExecutionInfo().getPagingState();
                        log.info("Paging response {}", nextPageState);
                        pagingStates.put(page + 1, nextPageState);
                        isFinalPage = nextPageState == null;
                        this.resultSet = resultSet;
                    }
                })
                .thenApply(this::parseResultSet)
                .thenApply(FXCollections::observableList);
    }

    private Statement getStatement() {
        Statement statement = new SimpleStatement(query);
        statement.setFetchSize(pageSize);
        return statement;
    }

    private List<DataObject> parseResultSet(ResultSet resultSet) {
        AtomicInteger start = new AtomicInteger(pageSize * page);
        int availableWithoutFetching = resultSet.getAvailableWithoutFetching();
        int currentPageSize = Math.min(availableWithoutFetching, pageSize);
        log.info("Page {} size {}", page, currentPageSize);
        return StreamSupport.stream(resultSet.spliterator(), false)
                .limit(currentPageSize)
                .map(row -> parseRow(start.getAndIncrement(), row))
                .collect(Collectors.toList());
    }

    private DataObject parseRow(int index, Row row) {
        DataObject dataObject = new DataObject(index);
        for (ColumnDefinitions.Definition definition : row.getColumnDefinitions()) {
            TypeCodec<Object> codec = CodecRegistry.DEFAULT_INSTANCE.codecFor(definition.getType());
            dataObject.set(definition.getName(), row.get(definition.getName(), codec));
        }
        return dataObject;
    }

    private List<TableColumn<DataObject, Object>> buildColumns() {
        List<TableColumn<DataObject, Object>> columns = new ArrayList<>();

        TableColumn<DataObject, Object> counterColumn = buildColumn(DataType.cint(), "#");
        counterColumn.setCellValueFactory(param -> {
            Integer object = param.getValue().getPosition();
            return new SimpleObjectProperty<>(object);
        });

        columns.add(counterColumn);

        tableMetadata.getColumns().forEach(columnMetadata -> {
            DataType type = columnMetadata.getType();
            TableColumn<DataObject, Object> column = buildColumn(type, columnMetadata.getName());
            column.setCellValueFactory(param -> {
                Object object = param.getValue().get(columnMetadata.getName());
                return new SimpleObjectProperty<>(object);
            });
            column.setOnEditCommit(event -> {
                log.debug("Updating row value with {}", event.getRowValue());
                clientAdapter.update(tableMetadata, event)
                        .whenComplete(
                                (aVoid, throwable) -> {
                                    if (throwable != null) {
                                        log.error(throwable.getMessage(), throwable);
                                    } else {
                                        log.debug("Updated : {}", event.getRowValue());
                                    }
                                });
            });
            columns.add(column);
        });
        return columns;
    }

    private TableColumn<DataObject, Object> buildColumn(DataType dataType, String label) {
        TableColumn<DataObject, Object> counterColumn = new TableColumn<>();
        Label counterColumnLabel = new Label(label);
        counterColumnLabel.setTooltip(new Tooltip(dataType.asFunctionParameterString()));

        TypeCodec<Object> counterCodec = CodecRegistry.DEFAULT_INSTANCE.codecFor(dataType);
        counterColumn.setCellFactory(cellFactory(counterCodec));
        counterColumn.setGraphic(counterColumnLabel);
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        float width = fontLoader.computeStringWidth(counterColumnLabel.getText(), counterColumnLabel.getFont());
        counterColumn.setMinWidth(width * 1.3);
        return counterColumn;
    }
}
