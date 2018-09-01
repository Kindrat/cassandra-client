package com.github.kindrat.cassandra;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.testng.annotations.BeforeSuite;

@SpringBootTest(classes = DaoTestConfiguration.class)
@TestExecutionListeners({TransactionalTestExecutionListener.class})
public abstract class SuiteSetup extends AbstractTestNGSpringContextTests {
    @BeforeSuite
    public void initContext() throws Exception {
        springTestContextPrepareTestInstance();
    }
}
