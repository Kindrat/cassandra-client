package com.github.kindrat.cassandra.client.service

import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import com.datastax.oss.driver.api.core.cql.QueryTrace
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata
import com.datastax.oss.driver.api.core.type.DataTypes
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry
import com.github.kindrat.cassandra.client.ui.DataObject
import com.github.kindrat.cassandra.client.ui.fx.TableColumns
import com.github.kindrat.cassandra.client.util.EvenMoreFutures.logErrorIfPresent
import com.github.kindrat.cassandra.client.util.EvenMoreFutures.loggingConsumer
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TableColumn
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function
import java.util.stream.StreamSupport
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.toList

private val logger = KotlinLogging.logger {}

class TableContext private constructor(
        val table: String,
        private val query: String,
        val tableMetadata: TableMetadata,
        private val clientAdapter: CassandraClientAdapter,
        private val pageSize: Int) {

    private val pagingStates: MutableMap<Int, ByteArray> = HashMap()
    val columns: List<TableColumn<DataObject, Any>> = buildColumns()
    private var page = 0
    private var currentPage: CompletionStage<ObservableList<DataObject>>? = null
    private val resultSet: AtomicReference<AsyncResultSet?> = AtomicReference(null)

    @Synchronized
    fun nextPage(): CompletionStage<ObservableList<DataObject>> {
        if (!hasNextPage()) {
            return currentPage ?: CompletableFuture.completedStage(FXCollections.emptyObservableList())
        }
        page++
        currentPage = resultSet.get()?.fetchNextPage()
                ?.thenApply { parseResultSet(it) }
                ?.thenApply { FXCollections.observableList(it) }
        return currentPage ?: CompletableFuture.completedStage(FXCollections.emptyObservableList())
    }

    @Synchronized
    fun hasPreviousPage(): Boolean {
        return page > 0
    }

    @Synchronized
    fun hasNextPage(): Boolean {
        return resultSet.get()?.hasMorePages() ?: false
    }

    @Synchronized
    fun previousPage(): CompletionStage<ObservableList<DataObject>> {
        page = max(0, page - 1)
        val loadedPage = loadPage(page)
        currentPage = loadedPage
        return loadedPage
    }

    @Synchronized
    private fun loadPage(page: Int): CompletionStage<ObservableList<DataObject>> {
        logger.debug("Loading page $page")
        val statement = statement
        val rawPagingState = pagingStates[page]
        if (rawPagingState != null) {
            val pagingState = ByteBuffer.wrap(rawPagingState)
            logger.debug("Loading page $page with pagination state $rawPagingState")
            statement.pagingState = pagingState
        }
        return clientAdapter.executeStatement(statement)
                .thenApply {
                    val executionInfo = it.executionInfo
                    executionInfo.warnings.forEach { message -> logger.warn(message) }
                    printQueryTrace(page, executionInfo.queryTraceAsync)
                    resultSet.set(it)
                    it
                }
                .thenApply { parseResultSet(it) }
                .thenApply { FXCollections.observableList(it) }
                .whenComplete(logErrorIfPresent<ObservableList<DataObject>, Throwable>("Page $page load failed"))
    }

    private val statement: SimpleStatement
        get() = SimpleStatement.newInstance(query)
                .setTracing(true)
                .setPageSize(pageSize)

    private fun parseResultSet(resultSet: AsyncResultSet): List<DataObject> {
        val start = AtomicInteger(pageSize * page)
        val availableWithoutFetching = resultSet.remaining()
        var currentPageSize = min(availableWithoutFetching, pageSize)
        val nextPageState = resultSet.executionInfo.pagingState
        logger.debug("Page $page size $currentPageSize")
        if (nextPageState != null) {
            val value = nextPageState.array()
            logger.debug("Page ${page + 1} paging state $value")
            pagingStates[page + 1] = value
        } else {
            logger.info("No pagination state for remaining entries. Fetching them within current page.")
            currentPageSize = availableWithoutFetching
        }
        return StreamSupport.stream(resultSet.currentPage().spliterator(), false)
                .limit(currentPageSize.toLong())
                .map { parseRow(start.getAndIncrement(), it) }
                .toList()
    }

    private fun parseRow(index: Int, row: Row): DataObject {
        val dataObject = DataObject(index)
        for (definition in row.columnDefinitions) {
            val codec = CodecRegistry.DEFAULT.codecFor<Any>(definition.type)
            dataObject[definition.name.toString()] = row.get(definition.name, codec)!!
        }
        return dataObject
    }

    private fun buildColumns(): List<TableColumn<DataObject, Any>> {
        val columns: MutableList<TableColumn<DataObject, Any>> = ArrayList()
        val counterColumn = TableColumns.buildColumn<DataObject>(DataTypes.INT, "#")
        counterColumn.setCellValueFactory { SimpleObjectProperty(it.value.position) }
        counterColumn.isEditable = false
        columns.add(counterColumn)
        tableMetadata.columns.values.forEach { metadata: ColumnMetadata ->
            val type = metadata.type
            val column = TableColumns.buildColumn<DataObject>(type, metadata.name.toString())
            column.setCellValueFactory { SimpleObjectProperty(it.value[metadata.name.toString()]) }
            column.setOnEditCommit {
                logger.debug("Updating row value with ${it.rowValue}")
                clientAdapter.update(tableMetadata, it)
                        .whenComplete(loggingConsumer(Function<Void, Any> { _ -> it.rowValue }))
            }
            columns.add(column)
        }
        return columns
    }

    private fun printQueryTrace(page: Int, resultFuture: CompletionStage<QueryTrace>) {
        resultFuture.whenCompleteAsync { trace: QueryTrace?, throwable: Throwable? ->
            trace?.let {
                logger.info("""Executed page $page query trace : [${it.tracingId}] 
                                 started at ${it.startedAt} and took ${it.durationMicros} μs 
                                 coordinator ${it.coordinatorAddress}
                                 request type ${it.requestType}
                                 parameters ${it.parameters}
                                 events ${it.events}""")
            }
            throwable?.let { logger.error("Query trace could not be read", it) }
        }
    }

    companion object {
        @JvmStatic
        fun fullTable(table: String, tableMetadata: TableMetadata, clientAdapter: CassandraClientAdapter,
                      pageSize: Int): TableContext {
            return TableContext(table, "select * from $table", tableMetadata, clientAdapter, pageSize)
        }

        @JvmStatic
        fun customView(table: String, query: String, tableMetadata: TableMetadata,
                       clientAdapter: CassandraClientAdapter, pageSize: Int): TableContext {
            return TableContext(table, query, tableMetadata, clientAdapter, pageSize)
        }
    }
}