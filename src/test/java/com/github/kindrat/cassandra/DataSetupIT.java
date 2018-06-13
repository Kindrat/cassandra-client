package com.github.kindrat.cassandra;

import com.datastax.driver.core.Cluster;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.config.AbstractClusterConfiguration;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.ClusterBuilderConfigurer;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.repository.support.SimpleCassandraRepository;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootTest(classes = DataSetupIT.DaoTestConfiguration.class)
@TestExecutionListeners({TransactionalTestExecutionListener.class})
public class DataSetupIT extends AbstractTestNGSpringContextTests {
    @Autowired
    private TestDao dao;

    @Before
    public void setUp() throws Exception {
        springTestContextPrepareTestInstance();
    }

    @Test
    public void deployTestData() {
        IntStream.rangeClosed(0, 20_000)
                .mapToObj(value -> new TestEntity(value, System.currentTimeMillis(), UUID.randomUUID().toString()))
                .forEach(dao::save);
    }

    @EntityScan("com.github.kindrat.cassandra")
    @ImportAutoConfiguration({CassandraRepositoriesAutoConfiguration.class, CassandraDataAutoConfiguration.class})
    @EnableCassandraRepositories(repositoryBaseClass = SimpleCassandraRepository.class)
    @AutoConfigureDataJpa
    public static class DaoTestConfiguration extends AbstractClusterConfiguration implements ClusterBuilderConfigurer {

        @Bean
        public CassandraSessionFactoryBean session(CassandraConverter converter, Cluster cluster) {
            CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
            session.setCluster(cluster);
            session.setConverter(converter);
            session.setKeyspaceName("test");
            SchemaAction schemaAction = SchemaAction.CREATE_IF_NOT_EXISTS;
            session.setSchemaAction(schemaAction);
            session.setStartupScripts(getStartupScripts());
            return session;
        }

        @Override
        public Cluster.Builder configure(Cluster.Builder clusterBuilder) {
            return clusterBuilder.withClusterName("default")
                    .withPort(9042);
        }
    }
}
