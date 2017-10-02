package com.github.kindrat.cassandra.client.service;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.github.kindrat.cassandra.client.exception.UrlSyntaxException;
import com.github.kindrat.cassandra.client.ui.DataObject;
import com.github.nginate.commons.lang.NStrings;
import javafx.scene.control.TableColumn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getField;

@Slf4j
@Service
@RequiredArgsConstructor
public class CassandraClientAdapter {
    private final AtomicReference<CassandraAdminTemplate> template = new AtomicReference<>();
    private final BeanFactory beanFactory;

    /**
     * Create new session factory bean for provided cassandra endpoint
     *
     * @param url      cassandra url containing host and port (localhost:9042)
     * @param keyspace cassandra keyspace
     * @param username cassandra username
     * @param password cassandra password
     * @return future providing new instance of {@link CassandraAdminTemplate template} to manage cassandra data
     */
    public CompletableFuture<CassandraAdminTemplate> connect(String url, String keyspace, String username,
            String password) {
        return CompletableFuture.supplyAsync(() -> {
            if (isNullOrEmpty(keyspace) || isNullOrEmpty(url)) {
                throw new UrlSyntaxException("Both url and keyspace are required");
            }
            String[] urlParts = url.split(":");
            if (urlParts.length != 2) {
                throw new UrlSyntaxException("Url should contain host:port");
            }
            int port;
            try {
                port = Integer.parseInt(urlParts[1]);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new UrlSyntaxException("Invalid port : " + urlParts[1]);
            }

            Cluster.Builder builder = Cluster.builder()
                    .addContactPoint(urlParts[0])
                    .withPort(port);

            if (StringUtils.isNoneBlank(username, password)) {
                builder.withCredentials(username, password);
            }

            Cluster cluster = builder.build();

            CassandraSessionFactoryBean factory =
                    beanFactory.getBean(CassandraSessionFactoryBean.class, cluster, keyspace);
            Field adminTemplateField = findField(CassandraSessionFactoryBean.class, "admin");
            adminTemplateField.setAccessible(true);
            CassandraAdminTemplate template = (CassandraAdminTemplate) getField(adminTemplateField, factory);
            this.template.set(template);
            return template;
        });
    }

    public long count(String table) {
        return template.get().count(table);
    }

    public CompletableFuture<ResultSet> getAll(String table) {
        return CompletableFuture.supplyAsync(() -> template.get().query(NStrings.format("select * from {}", table)));
    }

    public CompletableFuture<Void> update(TableMetadata metadata, TableColumn.CellEditEvent<DataObject, Object> event) {
        return CompletableFuture.supplyAsync(() -> {
            Stream<ColumnMetadata> primaryKey = metadata.getPrimaryKey().stream();
            Stream<ColumnMetadata> partitionKey = metadata.getPartitionKey().stream();

            Set<String> keyNames = Stream.concat(primaryKey, partitionKey)
                    .map(ColumnMetadata::getName)
                    .collect(toSet());

            int column = event.getTablePosition().getColumn();
            String updatedColumn = metadata.getColumns().get(column).getName();

            if (keyNames.contains(updatedColumn)) {
                log.warn("Can't update primary key : {}", updatedColumn);
            }

            Update update = QueryBuilder.update(metadata);
            update.with(QueryBuilder.set(updatedColumn, event.getNewValue()));

            for (String columnName : keyNames) {
                update.where(QueryBuilder.eq(columnName, event.getRowValue().get(columnName)));
            }

            log.info("Executing update : {}", update);

            template.get().execute(update);
            event.getRowValue().set(updatedColumn, event.getNewValue());
            return null;
        });
    }

    public CompletableFuture<ResultSet> execute(String cql) {
        return CompletableFuture.supplyAsync(() -> template.get().query(cql));
    }
}
