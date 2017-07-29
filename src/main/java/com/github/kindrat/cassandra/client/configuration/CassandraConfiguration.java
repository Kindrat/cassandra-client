package com.github.kindrat.cassandra.client.configuration;

import com.datastax.driver.core.Cluster;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.data.cassandra.config.CassandraEntityClassScanner;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.convert.CustomConversions;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.mapping.SimpleUserTypeResolver;

import java.util.Collections;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@EnableConfigurationProperties(CassandraProperties.class)
public class CassandraConfiguration implements BeanClassLoaderAware {

    private ClassLoader classLoader;

    @Bean
    @Lazy
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CassandraSessionFactoryBean session(Cluster cluster, String keyspace) throws ClassNotFoundException {
        CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
        session.setCluster(cluster);
        session.setConverter(cassandraConverter(cluster, keyspace));
        session.setKeyspaceName(keyspace);
        session.setSchemaAction(SchemaAction.NONE);
        return session;
    }

    @Bean
    @Lazy
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CassandraConverter cassandraConverter(Cluster cluster, String keyspace) throws ClassNotFoundException {
        return new MappingCassandraConverter(cassandraMapping(cluster, keyspace));
    }

    @Bean
    @Lazy
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CassandraMappingContext cassandraMapping(Cluster cluster, String keyspace) throws ClassNotFoundException {
        BasicCassandraMappingContext mappingContext = new BasicCassandraMappingContext();
        mappingContext.setBeanClassLoader(classLoader);
        mappingContext.setInitialEntitySet(CassandraEntityClassScanner.scan(getEntityBasePackages()));
        CustomConversions customConversions = customConversions();
        mappingContext.setCustomConversions(customConversions);
        mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
        mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(cluster, keyspace));
        return mappingContext;
    }

    @Bean
    public CustomConversions customConversions() {
        return new CustomConversions(Collections.emptyList());
    }

    private String[] getEntityBasePackages() {
        return new String[] { getClass().getPackage().getName() };
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
