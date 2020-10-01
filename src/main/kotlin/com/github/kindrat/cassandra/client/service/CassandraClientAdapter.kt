package com.github.kindrat.cassandra.client.service

import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata
import com.datastax.oss.driver.api.querybuilder.QueryBuilder
import com.datastax.oss.driver.api.querybuilder.relation.Relation
import com.github.kindrat.cassandra.client.ui.ConnectionData
import com.github.kindrat.cassandra.client.ui.DataObject
import javafx.scene.control.TableColumn
import mu.KotlinLogging
import org.springframework.beans.factory.BeanFactory
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.data.cassandra.config.CompressionType
import org.springframework.data.cassandra.config.CqlSessionFactoryBean
import org.springframework.data.cassandra.core.CassandraAdminTemplate
import org.springframework.data.cassandra.core.cql.SessionCallback
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicReference

private val logger = KotlinLogging.logger {}

@Service
@ConstructorBinding
class CassandraClientAdapter(private val beanFactory: BeanFactory) {
    private val templateReference = AtomicReference<CassandraAdminTemplate>()

    /**
     * Create new session factory bean for provided cassandra endpoint
     *
     * @param connectionData connection metadata
     * @return future providing new instance of [template][CassandraAdminTemplate] to manage cassandra data
     */
    fun connect(connectionData: ConnectionData): CompletableFuture<CassandraAdminTemplate> {
        return CompletableFuture.supplyAsync {
            val factory = beanFactory.getBean(CqlSessionFactoryBean::class.java, connectionData)
            val session = Objects.requireNonNull(factory.getObject(), "Cloud not create session")
            val adminTemplate = CassandraAdminTemplate(session)
            templateReference.set(adminTemplate)
            adminTemplate
        }
    }

    fun getAllKeyspaces(url: String, dc: String, user: String?, password: String?): CompletableFuture<List<String>> {
        val connectionData = ConnectionData(url, "system_schema", dc, user, password, CompressionType.NONE)
        return connect(connectionData)
                .thenApply { it.select("SELECT keyspace_name FROM keyspaces", String::class.java) }
    }

    fun getAll(table: String): CompletableFuture<ResultSet> {
        return CompletableFuture.supplyAsync {
            templateReference.get().cqlOperations.queryForResultSet("select * from $table")
        }
    }

    fun executeStatement(statement: SimpleStatement): CompletionStage<AsyncResultSet> {
        val callback = SessionCallback { it.executeAsync(statement) }
        return templateReference.get().cqlOperations.execute(callback)!!
    }

    fun update(metadata: TableMetadata, event: TableColumn.CellEditEvent<DataObject, Any>): CompletableFuture<Void> {
        return CompletableFuture.supplyAsync {
            val keyNames = metadata.primaryKey.toMutableList().plus(metadata.partitionKey)
                    .map { it.name }
                    .toSet()

            val column = event.tablePosition.column - 1
            val updatedColumn = metadata.columns.keys.toList()[column]
            if (keyNames.contains(updatedColumn)) {
                logger.warn("Can't update primary key : $updatedColumn")
            }

            val rowValue = event.rowValue
            val relations = keyNames.map {
                val value = rowValue[it.toString()]
                return@map Relation.column(it).isEqualTo(QueryBuilder.literal(value))
            }.toList()

            val update = QueryBuilder.update(metadata.name)
                    .setColumn(updatedColumn, QueryBuilder.literal(event.newValue))
                    .where(relations)
            logger.info("Executing update : ${update.asCql()}")
            templateReference.get().cqlOperations.execute(update.asCql())
            rowValue[updatedColumn.toString()] = event.newValue
            null
        }
    }

    fun count(table: String): Mono<Long> {
        return Mono.fromFuture(execute("select count(*) from $table limit 100000000"))
                .map { it.one()!! }
                .map { it.getLong(0) }
    }

    fun execute(cql: String): CompletableFuture<ResultSet> {
        return CompletableFuture.supplyAsync { templateReference.get().cqlOperations.queryForResultSet(cql) }
    }
}