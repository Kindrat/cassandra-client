package com.github.kindrat.cassandra.client.service.tasks

import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata
import com.github.kindrat.cassandra.client.model.CsvTargetMetadata
import com.github.kindrat.cassandra.client.service.CassandraClientAdapter
import com.github.kindrat.cassandra.client.service.TableContext.Companion.fullTable
import com.github.kindrat.cassandra.client.ui.DataObject
import javafx.collections.ObservableList
import mu.KotlinLogging
import org.apache.commons.csv.CSVPrinter
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.io.FileWriter
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class CsvWriteBackgroundTask(
        private val metadata: CsvTargetMetadata,
        private val tableMetadata: TableMetadata,
        private val clientAdapter: CassandraClientAdapter
) : AbstractTask() {

    override fun innerRun(): Flux<TaskStatistics> {
        val start = Instant.now()
        return clientAdapter.count(metadata.table)
                .flatMapMany { totalRows: Long ->
                    var percentRows = max(totalRows / 100, 1)
                    percentRows = min(percentRows, Int.MAX_VALUE.toLong())
                    val processedRows = AtomicLong()
                    val columns = tableMetadata.columns.keys.map { it.toString() }.toList()
                    val printer = getPrinter(columns)
                    val tableContext = fullTable(metadata.table, tableMetadata, clientAdapter, 3000)
                    Flux.create { fluxSink: FluxSink<CompletionStage<ObservableList<DataObject>>> ->
                        fluxSink.next(tableContext.previousPage())
                        while (tableContext.hasNextPage()) {
                            fluxSink.next(tableContext.nextPage())
                        }
                        fluxSink.complete()
                    }
                            .map { extractValue(it) }
                            .flatMapIterable { it }
                            .doOnNext { printRow(it, columns, printer) }
                            .buffer(percentRows.toInt())
                            .map { dataObjects: List<DataObject> ->
                                val duration = Duration.between(start, Instant.now())
                                val processed = processedRows.addAndGet(dataObjects.size.toLong())
                                val progress = processed.toDouble() / totalRows
                                TaskStatistics(TaskState.ACTIVE, start.toEpochMilli(), duration, progress)
                            }
                            .doFinally { closePrinter(printer) }
                }
                .doOnError { logger.error("Could not write CSV", it) }
    }

    private fun getPrinter(columns: List<String>): CSVPrinter {
        val format = metadata.format.withHeader(*columns.toTypedArray())
        return CSVPrinter(FileWriter(metadata.target), format)
    }

    private fun closePrinter(printer: CSVPrinter) {
        printer.close(true)
    }

    private fun extractValue(future: CompletionStage<ObservableList<DataObject>>): ObservableList<DataObject> {
        return future.toCompletableFuture().get()
    }

    private fun printRow(dataObject: DataObject, header: List<String>, printer: CSVPrinter) {
        val fields = header.stream().map { name: String? -> dataObject[name!!] }.toArray()
        printer.printRecord(*fields)
    }
}