package com.github.kindrat.cassandra.client.service.tasks;

import com.github.kindrat.cassandra.client.model.CsvTargetMetadata;
import com.github.kindrat.cassandra.client.service.CassandraClientAdapter;
import com.github.kindrat.cassandra.client.service.TableContext;
import com.github.kindrat.cassandra.client.ui.DataObject;
import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVPrinter;
import reactor.core.publisher.Flux;

import java.io.FileWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.github.kindrat.cassandra.client.service.TableContext.fullTable;
import static com.github.kindrat.cassandra.client.service.tasks.TaskState.ACTIVE;

@RequiredArgsConstructor
public class CsvWriteBackgroundTask extends AbstractTask {
    private final CsvTargetMetadata metadata;
    private final CassandraClientAdapter clientAdapter;

    @Override
    Flux<TaskStatistics> innerRun() {
        long startTimestamp = System.currentTimeMillis();
        Instant start = Instant.ofEpochMilli(startTimestamp);
        return clientAdapter.count(metadata.getTable())
                .flatMapMany(totalRows -> {
                    long percentRows = Math.max(totalRows / 100, 1);
                    percentRows = Math.min(percentRows, Integer.MAX_VALUE);
                    AtomicLong processedRows = new AtomicLong();
                    CSVPrinter printer = getPrinter();
                    TableContext tableContext = fullTable(metadata.getTable(), null, clientAdapter, 3000);
                    return Flux.<CompletableFuture<ObservableList<DataObject>>>create(
                            fluxSink -> {
                                while (tableContext.hasNextPage()) {
                                    fluxSink.next(tableContext.nextPage());
                                }
                                fluxSink.complete();
                            })
                            .map(this::extractValue)
                            .flatMapIterable(Function.identity())
                            .doOnNext(dataObject -> printRow(dataObject, printer))
                            .buffer((int) percentRows)
                            .map(dataObjects -> {
                                Duration duration = Duration.between(start, Instant.now());
                                long processed = processedRows.addAndGet(dataObjects.size());
                                double progress = (double) processed / totalRows;
                                return new TaskStatistics(ACTIVE, startTimestamp, duration, progress);
                            })
                            .doFinally(signalType -> closePrinter(printer));
                });
    }

    @SneakyThrows
    private CSVPrinter getPrinter() {
        return new CSVPrinter(new FileWriter(metadata.getTarget()), metadata.getFormat());
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
    private void printRow(DataObject dataObject, CSVPrinter printer) {
        printer.printRecord(dataObject);
    }
}
