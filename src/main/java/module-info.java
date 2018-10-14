open module com.github.kindrat.cassadra.client {
    requires javafx.fxml;
    requires javafx.controls;
    requires jdk.unsupported;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.core;
    requires spring.beans;
    requires spring.data.cassandra;
    requires spring.context;

    requires cassandra.driver.core;

    requires org.apache.commons.lang3;
    requires commons.lang;
    requires guava;

    requires static lombok;
}
