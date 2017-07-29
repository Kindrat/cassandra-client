package com.github.kindrat.cassandra.client.ui;

import com.datastax.driver.core.AbstractTableMetadata;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import com.github.nginate.commons.lang.NStrings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static javafx.collections.FXCollections.emptyObservableList;
import static javafx.collections.FXCollections.observableArrayList;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getField;

@Slf4j
public class MainController {

    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private View view;

    private CassandraAdminTemplate cassandraAdminTemplate;
    private Map<String, TableMetadata> tableMetadata;

    @FXML
    private ListView<String> tables;
    @FXML
    private Button plusBtn;
    @FXML
    private Button minusBtn;
    @FXML
    private Button applyBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button runBtn;
    @FXML
    private TextField queryTb;
    @FXML
    private Label eventLbl;
    @FXML
    private Label serverLbl;

    @PostConstruct
    public void init() {
        disable(plusBtn, minusBtn, queryTb, runBtn, tables, applyBtn, cancelBtn);
        queryTb.textProperty().addListener((observable, oldValue, newValue) -> runBtn.setDisable(newValue.isEmpty()));
        tables.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    @FXML
    public void onConnectClick(ActionEvent event) {
        beanFactory.getBean("newConnectionBox", (BiConsumer<String, String>) this::loadTables);
        log.info("Connect click event : {}", event);
    }

    @FXML
    public void onAboutClick(ActionEvent event) {
        beanFactory.getBean("aboutBox", Stage.class);
        log.info("About click event : {}", event);
    }

    @FXML
    public void onTableSelection(MouseEvent event) {
        ObservableList<String> selectedItems = tables.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            disable(plusBtn, minusBtn);
        } else {
            enable(plusBtn, minusBtn);
        }
    }

    @SneakyThrows
    private void loadTables(String url, String keyspace) {
        tables.setItems(emptyObservableList());
        serverLbl.setText("");
        disable(plusBtn, minusBtn, queryTb, runBtn, tables, applyBtn, cancelBtn);

        if (isNullOrEmpty(keyspace) || isNullOrEmpty(url)) {
            return;
        }
        String[] urlParts = url.split(":");
        if (urlParts.length != 2) {
            return;
        }
        int port;
        try {
            port = Integer.parseInt(urlParts[1]);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        fireLogEvent("Loading from {}/{}", url, keyspace);
        Cluster cluster = Cluster.builder()
                .addContactPoint(urlParts[0])
                .withPort(port)
                .build();
        CassandraSessionFactoryBean factory = beanFactory.getBean(CassandraSessionFactoryBean.class, cluster, keyspace);
        Field adminTemplateField = findField(CassandraSessionFactoryBean.class, "admin");
        adminTemplateField.setAccessible(true);
        cassandraAdminTemplate = (CassandraAdminTemplate) getField(adminTemplateField, factory);
        KeyspaceMetadata keyspaceMetadata = cassandraAdminTemplate.getKeyspaceMetadata();
        tableMetadata = keyspaceMetadata.getTables()
                .stream()
                .collect(toMap(AbstractTableMetadata::getName, identity()));
        tables.setItems(observableArrayList(tableMetadata.keySet()).sorted());
        serverLbl.setText(url + "/" + keyspace);
        fireLogEvent("Loaded tables from {}/{}", url, keyspace);
        enable(queryTb, tables);
    }

    private void disable(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(true);
        }
    }

    private void enable(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(false);
        }
    }

    private void fireLogEvent(String template, Object... args) {
        String message = NStrings.format(template, args);
        log.info(message);
        eventLbl.setText(message);
    }
}
