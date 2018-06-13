package com.github.kindrat.cassandra.client.ui;

import com.datastax.driver.core.AbstractTableMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.service.CassandraClientAdapter;
import com.github.kindrat.cassandra.client.service.TableContext;
import com.github.kindrat.cassandra.client.ui.editor.EventLogger;
import com.github.kindrat.cassandra.client.ui.editor.FilterTextField;
import com.github.kindrat.cassandra.client.ui.editor.PaginationPanel;
import com.github.kindrat.cassandra.client.ui.eventhandler.FilterBtnHandler;
import com.github.kindrat.cassandra.client.ui.eventhandler.TableClickEvent;
import com.github.kindrat.cassandra.client.ui.eventhandler.TextFieldButtonWatcher;
import com.github.kindrat.cassandra.client.ui.keylistener.TableCellCopyHandler;
import com.github.kindrat.cassandra.client.ui.window.editor.tables.TablePanel;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;

import static com.github.kindrat.cassandra.client.service.TableContext.customView;
import static com.github.kindrat.cassandra.client.service.TableContext.fullTable;
import static com.github.kindrat.cassandra.client.util.CqlUtil.*;
import static com.github.kindrat.cassandra.client.util.StreamUtils.toMap;
import static com.github.kindrat.cassandra.client.util.UIUtil.*;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

@Slf4j
public class MainController {

    private final int pageSize = 1000;
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
    private TablePanel tablePanel;
    @Autowired
    private EventLogger eventLogger;
    @Autowired
    private PaginationPanel paginationPanel;

    private Map<String, TableMetadata> tableMetadata;

    @FXML
    private MenuBar menu;
    @FXML
    private SplitPane mainWindow;
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

    @FXML
    private GridPane pagePane;
    @FXML
    private Button prevBtn;
    @FXML
    private Button nextBtn;

    @PostConstruct
    public void init() {
        Menu fileMenu = (Menu) beanFactory.getBean("fileMenu");
        Menu helpMenu = (Menu) beanFactory.getBean("helpMenu");

        mainWindow.getItems().add(0, tablePanel);

        menu.getMenus().addAll(fileMenu, helpMenu);

        queryTextField.textProperty().addListener(TextFieldButtonWatcher.wrap(runButton));
        queryTextField.setPromptText(localeService.getMessage("ui.editor.query.textbox.tooltip"));
        runButton.setTooltip(new Tooltip(localeService.getMessage("ui.editor.query.button.tooltip")));

        runButton.setOnAction(
                event -> {
                    String cqlQuery = queryTextField.getText();
                    if (isSelect(cqlQuery)) {
                        getSelectTable(cqlQuery).ifPresent(table -> {
                            showDataRows(customView(table, cqlQuery, null, clientAdapter, pageSize));
                        });
                    } else {
                        clientAdapter.execute(cqlQuery).whenComplete((rows, throwable) -> {
                            if (throwable != null) {
                                printError(throwable);
                            }
                        });
                    }
                }
        );
        tablePanel.setNewValueListener(table -> filterTextField.setTableMetadata(tableMetadata.get(table)));
        dataTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        dataTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dataTableView.getSelectionModel().setCellSelectionEnabled(true);
        dataTableView.setOnMouseClicked(new TableClickEvent<>(dataTableView));
        eventAnchor.getChildren().add(eventLogger);
        tableDataGridPane.add(paginationPanel, 0, 2);
        fillParent(eventLogger);
        eventLogger.setVisible(true);
        disable(queryTextField, runButton, tablePanel);
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

    public void showDDLForTable(String tableName) {
        tableDataGridPane.setVisible(false);
        ddlTextArea.setText(formatDDL(tableMetadata.get(tableName).toString()));
        ddlTextArea.setVisible(true);
    }

    public void showDataForTable(String tableName) {
        eventLogger.fireLogEvent("Loading data for {}", tableName);
        showDataRows(fullTable(tableName, tableMetadata.get(tableName), clientAdapter, pageSize));
    }

    private void showDataRows(TableContext context) {
        runLater(() -> {
            ddlTextArea.setVisible(false);

            dataTableView.getColumns().clear();
            dataTableView.getItems().clear();
            dataTableView.setEditable(true);
            dataTableView.getColumns().addAll(context.getColumns());
            log.info("Getting page");
            context.previousPage()
                    .whenComplete((data, throwable) -> {
                        if (throwable != null) {
                            printError(throwable);
                        } else {
                            dataTableView.setItems(data);
                            dataTableView.refresh();
                            filterButton.setOnAction(new FilterBtnHandler(filterTextField, dataTableView, data));
                            filterTextField.setOnAction(new FilterBtnHandler(filterTextField, dataTableView, data));
                            paginationPanel.applyOnTable(context, dataObjects -> {
                                dataTableView.setItems(dataObjects);
                                dataTableView.refresh();
                            });
                            tableDataGridPane.setVisible(true);
                        }
                    })
                    .thenRun(() -> log.info("Load finished"));
        });
    }

    public void loadTables(ConnectionData connection) {
        tableDataGridPane.setVisible(false);
        ddlTextArea.setVisible(false);

        eventLogger.clear();
        eventLogger.fireLogEvent("Connecting to {}/{} ...", connection.getUrl(), connection.getKeyspace());
        tablePanel.clear();
        disable(tablePanel, queryTextField, runButton);

        clientAdapter.connect(connection)
                .thenApply(CassandraAdminTemplate::getKeyspaceMetadata)
                .thenApply(KeyspaceMetadata::getTables)
                .thenApply(tables -> toMap(tables, AbstractTableMetadata::getName))
                .whenComplete((metadata, error) -> {
                    if (error != null) {
                        printError(error);
                    } else {
                        tableMetadata = metadata;
                        showTableNames(connection.getUrl(), connection.getKeyspace());
                    }
                });
    }

    private void showTableNames(String url, String keyspace) {
        runLater(() -> {
            tablePanel.showTables(observableArrayList(tableMetadata.keySet()).sorted());
            eventLogger.printServerName(url, keyspace);
            eventLogger.fireLogEvent("Loaded tables from {}/{}", url, keyspace);
            enable(queryTextField, tablePanel);
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
