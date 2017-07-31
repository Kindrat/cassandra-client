package com.github.kindrat.cassandra.client.ui;

import com.github.kindrat.cassandra.client.CassandraClientGUI;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class AbstractJavaFxApplicationSupport extends javafx.application.Application {
    private static String[] savedArgs;

    protected ConfigurableApplicationContext context;

    protected static void launchApp(Class<? extends AbstractJavaFxApplicationSupport> clazz, String[] args) {
        AbstractJavaFxApplicationSupport.savedArgs = args;
        javafx.application.Application.launch(clazz, args);
    }

    @Override
    public void init() throws Exception {
        context = new SpringApplicationBuilder(CassandraClientGUI.class).web(false).run(savedArgs);
        context.registerShutdownHook();
        context.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        context.close();
    }
}
