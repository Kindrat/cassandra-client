package com.github.kindrat.cassandra.client.service;

import com.datastax.driver.core.*;
import com.github.kindrat.cassandra.client.ui.DataObject;
import com.github.kindrat.cassandra.client.util.EvenMoreFutures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
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

import static com.github.kindrat.cassandra.client.ui.fx.TableColumns.buildColumn;
import static com.github.kindrat.cassandra.client.util.EvenMoreFutures.*;
import static com.github.nginate.commons.lang.NStrings.format;

@Slf4j
public class TableContext {

    private final Map<Integer, String> pagingStates = new HashMap<>();
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
        if (!hasNextPage()) {
            return currentPage;
        }
        page++;
        currentPage = toCompletable(resultSet.fetchMoreResults())
                .thenApply(this::parseResultSet)
                .thenApply(FXCollections::observableList);
        return currentPage;
    }

    @Synchronized
    public boolean hasPreviousPage() {
        return page > 0;
    }

    @Synchronized
    public boolean hasNextPage() {
        return !resultSet.isExhausted();
    }

    @Synchronized
    public CompletableFuture<ObservableList<DataObject>> previousPage() {
        page = Math.max(0, page - 1);
        currentPage = loadPage(page);
        return currentPage;
    }

    @Synchronized
    private CompletableFuture<ObservableList<DataObject>> loadPage(int page) {
        log.debug("Loading page {}", page);
        Statement statement = getStatement();
        String rawPagingState = pagingStates.get(page);
        if (rawPagingState != null) {
            PagingState pagingState = PagingState.fromString(rawPagingState);
            log.debug("Loading page {} with pagination state {}", page, rawPagingState);
            statement.setPagingState(pagingState);
        }
        return clientAdapter.executeStatement(statement)
                .thenApply(result -> {
                    ExecutionInfo executionInfo = result.getExecutionInfo();
                    executionInfo.getWarnings().forEach(log::warn);
                    printQueryTrace(page, executionInfo.getQueryTraceAsync());
                    this.resultSet = result;
                    return result;
                })
                .thenApply(this::parseResultSet)
                .thenApply(FXCollections::observableList)
                .whenComplete(logErrorIfPresent(format("Page {} load failed", page)));
    }

    private Statement getStatement() {
        Statement statement = new SimpleStatement(query);
        statement.setFetchSize(pageSize);
        return statement.enableTracing();
    }

    private List<DataObject> parseResultSet(ResultSet resultSet) {
        AtomicInteger start = new AtomicInteger(pageSize * page);
        int availableWithoutFetching = resultSet.getAvailableWithoutFetching();
        int currentPageSize = Math.min(availableWithoutFetching, pageSize);
        PagingState nextPageState = resultSet.getExecutionInfo().getPagingState();
        log.debug("Page {} size {}", page, currentPageSize);
        if (nextPageState != null) {
            String value = nextPageState.toString();
            log.debug("Page {} paging state {}", page + 1, value);
            pagingStates.put(page + 1, value);
        } else {
            log.info("No pagination state for remaining entries. Fetching them within current page.");
            currentPageSize = availableWithoutFetching;
        }
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
        counterColumn.setEditable(false);

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
                clientAdapter.update(tableMetadata, event).whenComplete(loggingConsumer(aVoid -> event.getRowValue()));
            });
            columns.add(column);
        });
        return columns;
    }

    private void printQueryTrace(int page, ListenableFuture<QueryTrace> listenableFuture) {
        EvenMoreFutures.toCompletable(listenableFuture)
                .whenCompleteAsync((trace, throwable) -> {
                    if (trace != null) {
                        log.info("Executed page {} query trace : [{}] " +
                                        "\n\t started at {} and took {} μs " +
                                        "\n\t coordinator {}" +
                                        "\n\t request type {}" +
                                        "\n\t parameters {}" +
                                        "\n\t events",
                                page, trace.getTraceId(), trace.getStartedAt(), trace.getDurationMicros(),
                                trace.getCoordinator(), trace.getRequestType(), trace.getParameters(),
                                trace.getEvents());
                    }
                    if (throwable != null) {
                        log.error("Query trace could not be read", throwable);
                    }
                });

    }
}
