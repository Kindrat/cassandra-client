package com.github.kindrat.cassandra;

import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.ReconnectionPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.ClusterBuilderConfigurer;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.repository.support.SimpleCassandraRepository;

import javax.annotation.Nonnull;

import static org.springframework.beans.BeanUtils.instantiateClass;

@EntityScan("com.github.kindrat.cassandra")
@EnableConfigurationProperties(CassandraProperties.class)
@Configuration
@RequiredArgsConstructor
@EnableCassandraRepositories(repositoryBaseClass = SimpleCassandraRepository.class)
@AutoConfigureDataJpa
public class DaoTestConfiguration extends AbstractCassandraConfiguration {

    private final CassandraProperties properties;

    @Override
    protected ClusterBuilderConfigurer getClusterBuilderConfigurer() {
        return clusterBuilder -> {
            clusterBuilder
                    .withClusterName(properties.getClusterName())
                    .withoutJMXReporting()
                    .withPort(properties.getPort());
            if (properties.getUsername() != null) {
                clusterBuilder.withCredentials(properties.getUsername(), properties.getPassword());
            }
            if (properties.getCompression() != null) {
                clusterBuilder.withCompression(properties.getCompression());
            }
            if (properties.getLoadBalancingPolicy() != null) {
                LoadBalancingPolicy policy = instantiateClass(properties.getLoadBalancingPolicy());
                clusterBuilder.withLoadBalancingPolicy(policy);
            }
            if (properties.getReconnectionPolicy() != null) {
                ReconnectionPolicy policy = instantiateClass(properties.getReconnectionPolicy());
                clusterBuilder.withReconnectionPolicy(policy);
            }
            if (properties.getRetryPolicy() != null) {
                RetryPolicy policy = instantiateClass(properties.getRetryPolicy());
                clusterBuilder.withRetryPolicy(policy);
            }
            if (properties.isSsl()) {
                clusterBuilder.withSSL();
            }
            return clusterBuilder;
        };
    }

    @Nonnull
    @Override
    protected String getContactPoints() {
        return String.join(",", properties.getContactPoints());
    }

    @Override
    protected QueryOptions getQueryOptions() {
        QueryOptions options = new QueryOptions();
        if (this.properties.getConsistencyLevel() != null) {
            options.setConsistencyLevel(this.properties.getConsistencyLevel());
        }
        if (this.properties.getSerialConsistencyLevel() != null) {
            options.setSerialConsistencyLevel(this.properties.getSerialConsistencyLevel());
        }
        options.setFetchSize(this.properties.getFetchSize());
        return options;
    }

    @Override
    protected SocketOptions getSocketOptions() {
        SocketOptions options = new SocketOptions();
        if (properties.getConnectTimeout() != null) {
            options.setConnectTimeoutMillis((int) properties.getConnectTimeout().toMillis());
        }
        if (properties.getReadTimeout() != null) {
            options.setReadTimeoutMillis((int) properties.getReadTimeout().toMillis());
        }
        return options;
    }

    @Override
    protected PoolingOptions getPoolingOptions() {
        return new PoolingOptions()
                .setMaxRequestsPerConnection(HostDistance.LOCAL, 32768)
                .setMaxRequestsPerConnection(HostDistance.REMOTE, 2000);
    }

    @Nonnull
    protected String getKeyspaceName() {
        return properties.getKeyspaceName();
    }
}
