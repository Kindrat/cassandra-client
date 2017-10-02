package com.github.kindrat.cassandra.client.ui;

import com.datastax.driver.core.*;
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.service.CassandraClientAdapter;
import com.github.kindrat.cassandra.client.ui.editor.EventLogger;
import com.github.kindrat.cassandra.client.ui.editor.FilterTextField;
import com.github.kindrat.cassandra.client.ui.editor.TableListContext;
import com.github.kindrat.cassandra.client.ui.eventhandler.FilterBtnHandler;
import com.github.kindrat.cassandra.client.ui.eventhandler.TableClickEvent;
import com.github.kindrat.cassandra.client.ui.eventhandler.TextFieldButtonWatcher;
import com.github.kindrat.cassandra.client.ui.keylistener.TableCellCopyHandler;
import com.github.kindrat.cassandra.client.ui.menu.file.ConnectionDataHandler;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.kindrat.cassandra.client.util.CqlUtil.*;
import static com.github.kindrat.cassandra.client.util.StreamUtils.toMap;
import static com.github.kindrat.cassandra.client.util.UIUtil.*;
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
    @Autowired
    private FilterTextField filterTextField;
    @Autowired
    private EventLogger eventLogger;

    private ContextMenu tableContext;
    private Map<String, TableMetadata> tableMetadata;

    @FXML
    private MenuBar menu;

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
    private AnchorPane editorAnchor;
    @FXML
    private TextArea ddlTextArea;
    @FXML
    private GridPane tableDataGridPane;
    @FXML
    private TableView<DataObject> dataTableView;
    @FXML
    private GridPane filterGrid;
    @FXML
    private Button filterButton;
    @FXML
    private AnchorPane eventAnchor;

    @PostConstruct
    public void init() {
        disable(plusButton, minusButton, queryTextField, runButton, tables, applyButton, cancelButton);
        Menu fileMenu = (Menu) beanFactory.getBean("fileMenu", (EventHandler<ActionEvent>) this::onConnectClick);
        Menu helpMenu = (Menu) beanFactory.getBean("helpMenu", (EventHandler<ActionEvent>) this::onAboutClick);

        menu.getMenus().addAll(fileMenu, helpMenu);

        queryTextField.textProperty().addListener(TextFieldButtonWatcher.wrap(runButton));
        queryTextField.setPromptText(localeService.getMessage("ui.editor.query.textbox.tooltip"));
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
        tables.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filterTextField.setTableMetadata(tableMetadata.get(newValue));
            }
            tableContext.hide();
        });
        tableContext = new TableListContext(localeService, () -> showDDLForTable(getSelectedTable()),
                () -> showDataForTable(getSelectedTable()));

        dataTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        dataTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dataTableView.getSelectionModel().setCellSelectionEnabled(true);
        dataTableView.setOnMouseClicked(new TableClickEvent(dataTableView));
        eventAnchor.getChildren().add(eventLogger);
        fillParent(eventLogger);
        eventLogger.setVisible(true);
    }

    private String getSelectedTable() {
        return tables.getSelectionModel().getSelectedItem();
    }

    public void onWindowLoad() {
        TableCellCopyHandler copyHandler = new TableCellCopyHandler(dataTableView);
        getAccelerators().put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY), copyHandler);
        getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_ANY),
                () -> {
                    if (filterTextField.isFocused()) {
                        filterTextField.suggestCompletion();
                    }
                });
        filterGrid.add(filterTextField, 0, 0);
        GridPane.setMargin(filterTextField, new Insets(0, 10, 0, 10));
    }

    private ObservableMap<KeyCombination, Runnable> getAccelerators() {
        return view.getPrimaryStage().getScene().getAccelerators();
    }

    private void showDDLForTable(String tableName) {
        tableDataGridPane.setVisible(false);
        ddlTextArea.setText(formatDDL(metadataFor(tableName).toString()));
        ddlTextArea.setVisible(true);
    }

    private void showDataForTable(String tableName) {
        long tableSize = clientAdapter.count(tableName);
        eventLogger.fireLogEvent("Loading data for {}", tableName);
        clientAdapter.getAll(tableName).whenComplete((rows, throwable) -> {
            if (throwable != null) {
                printError(throwable);
            } else {
                eventLogger.fireLogEvent("Loaded {} entries from {}", tableSize, tableName);
                showDataRows(tableName, rows);
            }
        });
    }

    private void showDataRows(String tableName, ResultSet resultSet) {
        runLater(() -> {
            ddlTextArea.setVisible(false);

            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();
            dataTableView.setEditable(true);

            metadataFor(tableName).getColumns().forEach(columnMetadata -> {
                DataType type = columnMetadata.getType();
                TypeCodec<Object> codec = CodecRegistry.DEFAULT_INSTANCE.codecFor(type);

                TableColumn<DataObject, Object> column = new TableColumn<>();
                Label columnLabel = new Label(columnMetadata.getName());
                columnLabel.setTooltip(new Tooltip(type.asFunctionParameterString()));
                column.setCellFactory(cellFactory(codec));
                column.setCellValueFactory(param -> {
                    Object object = param.getValue().get(columnMetadata.getName());
                    return new SimpleObjectProperty<>(object);
                });
                column.setOnEditCommit(event -> {
                    log.debug("Updating row value with {}", event.getRowValue());
                    clientAdapter.update(metadataFor(tableName), event)
                            .whenComplete(
                                    (aVoid, throwable) -> {
                                        if (throwable != null) {
                                            log.error(throwable.getMessage(), throwable);
                                        } else {
                                            log.debug("Updated : {}", event.getRowValue());
                                        }
                                    });
                });
                column.setGraphic(columnLabel);
                FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
                float width = fontLoader.computeStringWidth(columnLabel.getText(), columnLabel.getFont());
                column.setMinWidth(width * 1.3);
                dataTableView.getColumns().add(column);
            });

            List<DataObject> objects = resultSet.all().stream().map(row -> {
                DataObject dataObject = new DataObject();
                for (ColumnDefinitions.Definition definition : row.getColumnDefinitions()) {
                    TypeCodec<Object> codec = CodecRegistry.DEFAULT_INSTANCE.codecFor(definition.getType());
                    dataObject.set(definition.getName(), row.get(definition.getName(), codec));
                }
                return dataObject;
            }).collect(Collectors.toList());

            ObservableList<DataObject> original = FXCollections.observableArrayList(objects);
            dataTableView.setItems(original);
            dataTableView.refresh();
            filterButton.setOnAction(new FilterBtnHandler(filterTextField, dataTableView, original));
            filterTextField.setOnAction(new FilterBtnHandler(filterTextField, dataTableView, original));
            tableDataGridPane.setVisible(true);
        });
    }

    private Callback<TableColumn<DataObject, Object>, TableCell<DataObject, Object>> cellFactory(
            TypeCodec<Object> codec) {
        return TextFieldTableCell.forTableColumn(new StringConverter<Object>() {
            @Override
            public String toString(Object object) {
                return codec.format(object);
            }

            @Override
            public Object fromString(String string) {
                return codec.parse(string);
            }
        });
    }

    private TableMetadata metadataFor(String tableName) {
        return tableMetadata.get(tableName);
    }

    private void onConnectClick(ActionEvent event) {
        beanFactory.getBean("newConnectionBox", (ConnectionDataHandler) this::loadTables);
    }

    private void onAboutClick(ActionEvent event) {
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
            tableContext.show(tables, event.getScreenX(), event.getScreenY());
        }
    }


    private void loadTables(String url, String keyspace, String username, String password) {
        tableDataGridPane.setVisible(false);
        ddlTextArea.setVisible(false);

        eventLogger.clear();
        eventLogger.fireLogEvent("Connecting to {}/{} ...", url, keyspace);
        tables.setItems(emptyObservableList());
        disable(plusButton, minusButton, queryTextField, runButton, tables, applyButton, cancelButton);

        clientAdapter.connect(url, keyspace, username, password)
                .thenApply(CassandraAdminTemplate::getKeyspaceMetadata)
                .thenApply(KeyspaceMetadata::getTables)
                .thenApply(tables -> toMap(tables, AbstractTableMetadata::getName))
                .whenComplete((metadata, error) -> {
                    if (error != null) {
                        printError(error);
                    } else {
                        tableMetadata = metadata;
                        showTableNames(url, keyspace);
                    }
                });
    }

    private void showTableNames(String url, String keyspace) {
        runLater(() -> {
            tables.setItems(observableArrayList(tableMetadata.keySet()).sorted());
            eventLogger.printServerName(url, keyspace);
            eventLogger.fireLogEvent("Loaded tables from {}/{}", url, keyspace);
            enable(queryTextField, tables);
        });
    }


    private void printError(Throwable t) {
        runLater(() -> {
            tableDataGridPane.setVisible(false);
            ddlTextArea.setText(ExceptionUtils.getStackTrace(t));
            ddlTextArea.setVisible(true);
        });
    }
}
