package com.github.kindrat.cassandra.client.ui;

import com.datastax.driver.core.AbstractTableMetadata;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.TypeCodec;
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.service.CassandraClientAdapter;
import com.github.kindrat.cassandra.client.ui.editor.TableListContext;
import com.github.kindrat.cassandra.client.ui.eventhandler.FilterBtnHandler;
import com.github.kindrat.cassandra.client.ui.eventhandler.TextFieldButtonWatcher;
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
import org.springframework.data.cassandra.core.CassandraAdminTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.github.kindrat.cassandra.client.util.CqlUtil.getSelectTable;
import static com.github.kindrat.cassandra.client.util.CqlUtil.isSelect;
import static com.github.kindrat.cassandra.client.util.StreamUtils.toMap;
import static com.github.nginate.commons.lang.NStrings.format;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.emptyObservableList;
import static javafx.collections.FXCollections.observableArrayList;

@Slf4j
public class MainController {

    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private MessageByLocaleService localeService;
    @Autowired
    private CassandraClientAdapter clientAdapter;
    @Autowired
    private View view;

    private ContextMenu tableContext;
    private Map<String, TableMetadata> tableMetadata;

    @FXML
    private ListView<String> tables;
    @FXML
    private Button plusButton;
    @FXML
    private Button minusButton;
    @FXML
    private Button applyButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button runButton;
    @FXML
    private TextField queryTextField;
    @FXML
    private Label eventLabel;
    @FXML
    private Label serverLabel;
    @FXML
    private AnchorPane editorAnchor;
    @FXML
    private TextArea ddlTextArea;
    @FXML
    private GridPane tableDataGridPane;
    @FXML
    private TableView<Row> dataTableView;
    @FXML
    private TextField filterTextField;
    @FXML
    private Button filterButton;

    @PostConstruct
    public void init() {
        disable(plusButton, minusButton, queryTextField, runButton, tables, applyButton, cancelButton);
        queryTextField.textProperty().addListener(TextFieldButtonWatcher.wrap(runButton));
        queryTextField.setTooltip(new Tooltip(localeService.getMessage("ui.editor.query.textbox.tooltip")));
        runButton.setTooltip(new Tooltip(localeService.getMessage("ui.editor.query.button.tooltip")));

        runButton.setOnAction(
                event -> {
                    String cqlQuery = queryTextField.getText();
                    clientAdapter.execute(cqlQuery).whenComplete((rows, throwable) -> {
                        if (throwable != null) {
                            printError(throwable);
                        } else if (isSelect(cqlQuery)) {
                            getSelectTable(cqlQuery).ifPresent(table -> showDataRows(table, rows));
                        }
                    });
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
        dataTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        dataTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dataTableView.getSelectionModel().setCellSelectionEnabled(true);
    }

    public void onWindowLoad() {
        TableCellCopyHandler copyHandler = new TableCellCopyHandler(dataTableView);
        getAccelerators().put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY), copyHandler);
    }

    private ObservableMap<KeyCombination, Runnable> getAccelerators() {
        return view.getPrimaryStage().getScene().getAccelerators();
    }

    private void showDDLForTable(String tableName) {
        tableDataGridPane.setVisible(false);

        TableMetadata tableMetadata = getTableMetadata(tableName);
        String ddl = tableMetadata.toString();

        String formattedDdl = DDLFormatterImpl.INSTANCE.format(ddl)
                .replaceAll("AND", "\n\tAND");
        ddlTextArea.setText(formattedDdl);
        ddlTextArea.setVisible(true);
    }

    private void showDataForTable(String tableName) {
        long tableSize = clientAdapter.count(tableName);
        fireLogEvent("Loading data for {}", tableName);
        clientAdapter.getAll(tableName).whenComplete((rows, throwable) -> {
            if (throwable != null) {
                printError(throwable);
            } else {
                fireLogEvent("Loaded {} entries from {}", tableSize, tableName);
                showDataRows(tableName, rows);
            }
        });
    }

    private void showDataRows(String tableName, ResultSet resultSet) {
        runLater(() -> {
            ddlTextArea.setVisible(false);
            TableMetadata tableMetadata = getTableMetadata(tableName);

            //noinspection unchecked
            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();

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
                dataTableView.getColumns().add(column);
            });

            ObservableList<Row> original = FXCollections.observableArrayList(resultSet.all());
            dataTableView.setItems(original);
            filterButton.setOnAction(new FilterBtnHandler(filterTextField, dataTableView, original));
            tableDataGridPane.setVisible(true);
        });
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
            disable(plusButton, minusButton);
        } else {
            enable(plusButton, minusButton);
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
        serverLabel.setText("");
        disable(plusButton, minusButton, queryTextField, runButton, tables, applyButton, cancelButton);

        clientAdapter.connect(url, keyspace)
                .thenApply(CassandraAdminTemplate::getKeyspaceMetadata)
                .thenApply(KeyspaceMetadata::getTables)
                .thenApply(tables -> toMap(tables, AbstractTableMetadata::getName))
                .whenComplete((metadata, error) -> {
                    if (error != null) {
                        printError(error);
                    } else {
                        runLater(() -> {
                            tableMetadata = metadata;
                            tables.setItems(observableArrayList(tableMetadata.keySet()).sorted());
                            serverLabel.setText(url + "/" + keyspace);
                            fireLogEvent("Loaded tables from {}/{}", url, keyspace);
                            enable(queryTextField, tables);
                        });
                    }
                });
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
        runLater(() -> {
            String message = format(template, args);
            log.info(message);
            eventLabel.setText(message);
        });
    }

    private void clear(Pane pane) {
        ObservableList<Node> children = pane.getChildren();
        children.clear();
    }

    private void printError(Throwable t) {
        runLater(() -> {
            tableDataGridPane.setVisible(false);
            ddlTextArea.setText(ExceptionUtils.getStackTrace(t));
            ddlTextArea.setVisible(true);
        });
    }
}
