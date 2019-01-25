package com.github.kindrat.cassandra.client.service.tasks;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.TableMetadata;
import com.github.kindrat.cassandra.client.model.CsvTargetMetadata;
import com.github.kindrat.cassandra.client.service.CassandraClientAdapter;
import com.github.kindrat.cassandra.client.service.TableContext;
import com.github.kindrat.cassandra.client.ui.DataObject;
import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import reactor.core.publisher.Flux;

import java.io.FileWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.kindrat.cassandra.client.service.TableContext.fullTable;
import static com.github.kindrat.cassandra.client.service.tasks.TaskState.ACTIVE;

@Slf4j
@RequiredArgsConstructor
public class CsvWriteBackgroundTask extends AbstractTask {
    private final CsvTargetMetadata metadata;
    private final TableMetadata tableMetadata;
    private final CassandraClientAdapter clientAdapter;

    @Override
    Flux<TaskStatistics> innerRun() {
        Instant start = Instant.now();
        return clientAdapter.count(metadata.getTable())
                .flatMapMany(totalRows -> {
                    long percentRows = Math.max(totalRows / 100, 1);
                    percentRows = Math.min(percentRows, Integer.MAX_VALUE);
                    AtomicLong processedRows = new AtomicLong();
                    List<String> columns = tableMetadata.getColumns().stream()
                            .map(ColumnMetadata::getName).collect(Collectors.toList());
                    CSVPrinter printer = getPrinter(columns);
                    TableContext tableContext = fullTable(metadata.getTable(), tableMetadata, clientAdapter, 3000);
                    return Flux.<CompletableFuture<ObservableList<DataObject>>>create(
                            fluxSink -> {
                                fluxSink.next(tableContext.previousPage());
                                while (tableContext.hasNextPage()) {
                                    fluxSink.next(tableContext.nextPage());
                                }
                                fluxSink.complete();
                            })
                            .map(this::extractValue)
                            .flatMapIterable(Function.identity())
                            .doOnNext(dataObject -> printRow(dataObject, columns, printer))
                            .buffer((int) percentRows)
                            .map(dataObjects -> {
                                Duration duration = Duration.between(start, Instant.now());
                                long processed = processedRows.addAndGet(dataObjects.size());
                                double progress = (double) processed / totalRows;
                                return new TaskStatistics(ACTIVE, start.toEpochMilli(), duration, progress);
                            })
                            .doFinally(signalType -> closePrinter(printer));
                })
                .doOnError(throwable -> log.error("Could not write CSV", throwable));
    }

    @SneakyThrows
    private CSVPrinter getPrinter(List<String> columns) {
        CSVFormat format = metadata.getFormat().withHeader(columns.toArray(new String[0]));
        return new CSVPrinter(new FileWriter(metadata.getTarget()), format);
    }

    @SneakyThrows
    private void closePrinter(CSVPrinter printer) {
        printer.close(true);
    }

    @SneakyThrows
    private ObservableList<DataObject> extractValue(CompletableFuture<ObservableList<DataObject>> future) {
        return future.get();
    }

    @SneakyThrows
    private void printRow(DataObject dataObject, List<String> header, CSVPrinter printer) {
        Object[] fields = header.stream().map(dataObject::get).toArray();
        printer.printRecord(fields);
    }
}
