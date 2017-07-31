package com.github.kindrat.cassandra.client.ui;

import com.datastax.driver.core.AbstractTableMetadata;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.TypeCodec;
import com.github.kindrat.cassandra.client.filter.DataFilter;
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.ui.editor.TableListContext;
import com.github.kindrat.cassandra.client.ui.keylistener.TableCellCopyHandler;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.engine.jdbc.internal.DDLFormatterImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.nginate.commons.lang.NStrings.format;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static javafx.collections.FXCollections.emptyObservableList;
import static javafx.collections.FXCollections.observableArrayList;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getField;

@Slf4j
public class MainController {
    private static final Pattern tableInSelect = Pattern.compile(
            "([sS][eE][lL][eE][cC][tT])(\\ \\S+\\ )([fF][rR][oO][mM]\\ )(\\S+)");
    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private MessageByLocaleService localeService;
    @Autowired
    private View view;

    private CassandraAdminTemplate cassandraAdminTemplate;
    private ContextMenu tableContext;
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
    @FXML
    private AnchorPane editorAnchor;
    @FXML
    private TextArea ddlTextArea;
    @FXML
    private GridPane tableDataPnl;
    @FXML
    private TableView<Row> dataTbl;
    @FXML
    private TextField filterTb;
    @FXML
    private Button filterBtn;

    @PostConstruct
    public void init() {
        disable(plusBtn, minusBtn, queryTb, runBtn, tables, applyBtn, cancelBtn);
        queryTb.textProperty().addListener((observable, old, newValue) -> runBtn.setDisable(newValue.isEmpty()));
        runBtn.setOnAction(
                event -> {
                    String rawQuery = queryTb.getText();
                    try {
                        ResultSet resultSet = cassandraAdminTemplate.query(rawQuery);
                        Matcher matcher = tableInSelect.matcher(rawQuery);
                        if (matcher.find()) {
                            String tableName = matcher.group(4);
                            showDataRows(tableName, resultSet.all());
                        }
                    } catch (Exception e) {
                        fireLogEvent("Statement failed : {}", e.getMessage());
                        dataTbl.setVisible(false);
                        ddlTextArea.setText(ExceptionUtils.getStackTrace(e));
                        ddlTextArea.setVisible(true);
                    }
                }
        );
        tables.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tables.setOnContextMenuRequested(this::onTableContextMenu);
        tableContext = new TableListContext(localeService,
                () -> {
                    String tableName = tables.getSelectionModel().getSelectedItem();
                    showDDLForTable(tableName);
                },
                () -> {
                    String tableName = tables.getSelectionModel().getSelectedItem();
                    showDataForTable(tableName);
                });
        dataTbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        dataTbl.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dataTbl.getSelectionModel().setCellSelectionEnabled(true);
    }

    public void onWindowLoad() {
        TableCellCopyHandler copyHandler = new TableCellCopyHandler(dataTbl);
        getAccelerators().put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY), copyHandler);
    }

    private ObservableMap<KeyCombination, Runnable> getAccelerators() {
        return view.getPrimaryStage().getScene().getAccelerators();
    }

    private void showDDLForTable(String tableName) {
        tableDataPnl.setVisible(false);

        TableMetadata tableMetadata = getTableMetadata(tableName);
        String ddl = tableMetadata.toString();

        String formattedDdl = DDLFormatterImpl.INSTANCE.format(ddl)
                .replaceAll("AND", "\n\tAND");
        ddlTextArea.setText(formattedDdl);
        ddlTextArea.setVisible(true);
    }

    private void showDataForTable(String tableName) {
        long tableSize = cassandraAdminTemplate.count(tableName);
        fireLogEvent("Loading data for {}", tableName);
        ResultSet resultSet = cassandraAdminTemplate.query(format("select * from {}", tableName));
        List<Row> loadedRows = resultSet.all();
        fireLogEvent("Loaded {} entries from {}", tableSize, tableName);

        showDataRows(tableName, loadedRows);
    }

    private void showDataRows(String tableName, List<Row> loadedRows) {
        ddlTextArea.setVisible(false);
        TableMetadata tableMetadata = getTableMetadata(tableName);

        //noinspection unchecked
        dataTbl.getColumns().clear();
        dataTbl.getItems().clear();

        tableMetadata.getColumns().forEach(columnMetadata -> {
            DataType type = columnMetadata.getType();

            TableColumn<Row, Object> column = new TableColumn<>();
            Label columnLabel = new Label(columnMetadata.getName());
            columnLabel.setTooltip(new Tooltip(type.asFunctionParameterString()));
            column.setCellValueFactory(param -> {
                TypeCodec<Object> codec = CodecRegistry.DEFAULT_INSTANCE.codecFor(type);
                Object object = param.getValue().get(columnMetadata.getName(), codec);
                return new SimpleObjectProperty<>(object);
            });
            column.setGraphic(columnLabel);
            dataTbl.getColumns().add(column);
        });

        ObservableList<Row> original = FXCollections.observableArrayList(loadedRows);
        dataTbl.setItems(original);

        filterBtn.setOnAction(event -> {
            if (filterTb.getText().isEmpty()) {
                if (dataTbl.getItems().size() != original.size()) {
                    dataTbl.setItems(original);
                }
            } else {
                DataFilter.parse(filterTb.getText()).ifPresent(dataFilter -> {
                    List<Row> filtered = original.stream().filter(dataFilter).collect(Collectors.toList());
                    dataTbl.setItems(FXCollections.observableArrayList(filtered));
                });
            }
        });

        tableDataPnl.setVisible(true);
    }

    private TableMetadata getTableMetadata(String tableName) {
        return this.tableMetadata.get(tableName);
    }

    @FXML
    public void onConnectClick(ActionEvent event) {
        beanFactory.getBean("newConnectionBox", (BiConsumer<String, String>) this::loadTables);
    }

    @FXML
    public void onAboutClick(ActionEvent event) {
        beanFactory.getBean("aboutBox", Stage.class);
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

    private void onTableContextMenu(ContextMenuEvent event) {
        ObservableList<String> selectedItems = tables.getSelectionModel().getSelectedItems();
        if (!selectedItems.isEmpty()) {
            String selectedItem = selectedItems.get(0);
            tableContext.show(tables, event.getScreenX(), event.getScreenY());
        }
    }


    @SneakyThrows
    private void loadTables(String url, String keyspace) {
        fireLogEvent("Connecting to {}/{} ...", url, keyspace);
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
        String message = format(template, args);
        log.info(message);
        eventLbl.setText(message);
    }

    private void clear(Pane pane) {
        ObservableList<Node> children = pane.getChildren();
        children.clear();
    }
}
