package com.github.kindrat.cassandra.client.service;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.github.kindrat.cassandra.client.exception.UrlSyntaxException;
import com.github.nginate.commons.lang.NStrings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Strings.isNullOrEmpty;
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
     * @return future providing new instance of {@link CassandraAdminTemplate template} to manage cassandra data
     */
    public CompletableFuture<CassandraAdminTemplate> connect(String url, String keyspace) {
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

            Cluster cluster = Cluster.builder()
                    .addContactPoint(urlParts[0])
                    .withPort(port)
                    .build();

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

    public CompletableFuture<Void> update(TableMetadata metadata, Row row) {
        return CompletableFuture.supplyAsync(() -> {
            List<ColumnMetadata> primaryKey = metadata.getPrimaryKey();
            List<ColumnMetadata> partitionKey = metadata.getPartitionKey();

            Update update = QueryBuilder.update(metadata);
            for (ColumnMetadata columnMetadata : metadata.getColumns()) {
                if (!partitionKey.contains(columnMetadata) && !partitionKey.contains(columnMetadata)) {
                    String name = columnMetadata.getName();
                    update.with(QueryBuilder.add(name, row.getObject(name)));
                }
            }

            for (ColumnMetadata columnMetadata : primaryKey) {
                String name = columnMetadata.getName();
                update.where(QueryBuilder.eq(name, row.getObject(name)));
            }

            for (ColumnMetadata columnMetadata : partitionKey) {
                String name = columnMetadata.getName();
                update.where(QueryBuilder.eq(name, row.getObject(name)));
            }

            template.get().execute(update);
            return null;
        });
    }

    public CompletableFuture<ResultSet> execute(String cql) {
        return CompletableFuture.supplyAsync(() -> template.get().query(cql));
    }
}
