package com.github.kindrat.cassandra.client.ui.widget.tableedit;

import com.datastax.driver.core.DataType;
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import com.github.kindrat.cassandra.client.ui.fx.CellValueFactory;
import com.github.kindrat.cassandra.client.ui.fx.component.ToggleSwitch;
import com.github.kindrat.cassandra.client.util.UIUtil;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.PickResult;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static com.github.kindrat.cassandra.client.ui.fx.TableColumns.*;
import static com.github.kindrat.cassandra.client.util.UIUtil.computeTextContainerWidth;
import static com.github.kindrat.cassandra.client.util.UIUtil.fillParent;
import static javafx.beans.binding.Bindings.size;

@Slf4j
public class TableEditWidget extends Stage {
    private final MessageByLocaleService localeService;
    private final UIProperties properties;
    private final Pane editorHolder = new Pane();

    private final ViewDataConverter viewDataConverter = new ViewDataConverter();
    private final ObservableList<TableRowEntry> rows = FXCollections.observableArrayList();
    private final TextArea textView = new TextArea();
    private final TableEditWidgetContext widgetContext;
    private final TextArea tableNameArea;
    private final TableView<TableRowEntry> tableView;

    public TableEditWidget(Stage parent, MessageByLocaleService localeService, UIProperties properties) {
        this.localeService = localeService;
        this.properties = properties;
        this.tableView = buildTableView();
        this.tableNameArea = buildTableArea();

        ReadOnlyIntegerProperty selectedIndexProperty = tableView.getSelectionModel().selectedIndexProperty();
        widgetContext = new TableEditWidgetContext(size(rows), selectedIndexProperty);
        widgetContext.onDelete(() -> rows.remove(selectedIndexProperty.getValue().intValue()));
        widgetContext.onAddAbove(() -> {
            int index = Math.max(0, selectedIndexProperty.getValue() - 1);
            rows.add(index, buildDefaultRow());
        });
        widgetContext.onAddBelow(() -> {
            Integer selected = selectedIndexProperty.getValue();
            if (selected == rows.size()) {
                rows.add(buildDefaultRow());
            } else {
                rows.add(selected + 1, buildDefaultRow());
            }
        });
        widgetContext.onMoveUp(this::moveUp);
        widgetContext.onMoveDown(this::moveDown);

        setTitle(localeService.getMessage("ui.editor.table_editor.title"));
        getIcons().add(new Image("cassandra_ico.png"));
        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setScene(buildScene());

        reset();
        show();
    }

    private void reset() {
        rows.clear();
        if (rows.isEmpty()) {
            rows.add(buildDefaultRow());
        }
    }

    private Scene buildScene() {
        AnchorPane container = new AnchorPane();
        VBox editor = new VBox();
        container.getChildren().add(editor);
        fillParent(editor);

        Scene dialogScene = new Scene(container, properties.getTableEditorWidth(), properties.getTableEditorHeight());
        Label sliderLabel = new Label(localeService.getMessage("ui.editor.table_editor.slider.label"));
        String textViewLabel = localeService.getMessage("ui.editor.table_editor.slider.text_view");
        String tableViewLabel = localeService.getMessage("ui.editor.table_editor.slider.table_view");
        ToggleSwitch toggleSwitch = new ToggleSwitch(textViewLabel, tableViewLabel);
        HBox sliderBox = new HBox(sliderLabel, toggleSwitch);
        sliderBox.setSpacing(20.0);
        sliderBox.setAlignment(Pos.CENTER_RIGHT);

        String tableName = localeService.getMessage("ui.editor.table_editor.table_name.label");
        Label newTableNameLabel = new Label(tableName);
        newTableNameLabel.setMinWidth(Region.USE_PREF_SIZE);
        newTableNameLabel.setMaxWidth(Region.USE_PREF_SIZE);
        newTableNameLabel.setPrefWidth(computeTextContainerWidth(tableName, newTableNameLabel.getFont()));
        HBox tableNameBox = new HBox(newTableNameLabel, tableNameArea);
        tableNameBox.setSpacing(20.0);
        tableNameBox.setPadding(new Insets(2.0, 2.0, 2.0, 10.0));
        tableNameBox.setAlignment(Pos.CENTER_LEFT);

        toggleSwitch.onEnabled(() -> {
            editorHolder.getChildren().clear();
            textView.setText(viewDataConverter.toText(rows, tableNameArea.getText()));
            tableNameBox.setVisible(false);
            editorHolder.getChildren().add(textView);
        });

        toggleSwitch.onDisabled(() -> {
            editorHolder.getChildren().clear();
            Map.Entry<String, Collection<TableRowEntry>> ddlData = viewDataConverter.fromText(textView.getText());
            tableNameArea.setText(ddlData.getKey());
            rows.clear();
            rows.addAll(ddlData.getValue());
            tableNameBox.setVisible(true);
            editorHolder.getChildren().add(tableView);
        });

        Button createButton = new Button(localeService.getMessage("ui.editor.table_editor.buttons.create"));
        Button resetButton = new Button(localeService.getMessage("ui.editor.table_editor.buttons.reset"));
        resetButton.setOnAction(event -> reset());
        HBox buttons = new HBox(20.0, createButton, resetButton);
        buttons.setAlignment(Pos.CENTER);

        VBox headerBox = new VBox(sliderBox, tableNameBox);
        ObservableList<Node> children = editor.getChildren();
        children.add(headerBox);
        children.add(editorHolder);
        children.add(buttons);
        editorHolder.getChildren().add(tableView);

        dialogScene.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            tableView.setPrefHeight(newValue.doubleValue() - headerBox.getHeight() - buttons.getHeight());
            textView.setPrefHeight(newValue.doubleValue() - headerBox.getHeight() - buttons.getHeight());
        });
        dialogScene.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            UIUtil.setWidth(editorHolder, newValue);
            UIUtil.setWidth(tableView, newValue);
            UIUtil.setWidth(textView, newValue);
            UIUtil.setWidth(buttons, newValue);
        });

        UIUtil.setWidth(textView, properties.getTableEditorWidth());
        setOnShown(event -> {
            double offset = headerBox.getHeight() + buttons.getHeight();
            textView.setPrefHeight(properties.getTableEditorHeight() - offset);
        });
        return dialogScene;
    }

    private TableView<TableRowEntry> buildTableView() {
        rows.add(buildDefaultRow());
        TableView<TableRowEntry> view = new TableView<>(rows);

        TableColumn<TableRowEntry, Object> nameColumn = buildColumn(DataType.text(), "Name");
        nameColumn.setCellValueFactory(CellValueFactory.create(TableRowEntry::getName));
        nameColumn.setOnEditCommit(event -> event.getRowValue().setName(event.getNewValue().toString()));
        bindTableColumnWidth(nameColumn, this, 0.3);

        TableColumn<TableRowEntry, DataType.Name> typeColumn = buildColumn(DataType.Name.class, "Type");
        typeColumn.setCellValueFactory(CellValueFactory.create(TableRowEntry::getType));
        typeColumn.setOnEditCommit(event -> event.getRowValue().setType(event.getNewValue()));
        bindTableColumnWidth(typeColumn, this, 0.25);

        TableColumn<TableRowEntry, Boolean> partitionKeyColumn =
                buildCheckBoxColumn("Partition Key", TableRowEntry::getIsPartitionKeyProperty);
        bindTableColumnWidth(partitionKeyColumn, this, 0.1);

        TableColumn<TableRowEntry, Boolean> clusteringKeyColumn = buildCheckBoxColumn("Clustering Key",
                TableRowEntry::getIsClusteringKeyProperty);
        bindTableColumnWidth(clusteringKeyColumn, this, 0.1);

        TableColumn<TableRowEntry, Boolean> indexColumn = buildCheckBoxColumn("Index",
                TableRowEntry::getHasIndexProperty);
        bindTableColumnWidth(indexColumn, this, 0.1);

        view.getColumns().add(nameColumn);
        view.getColumns().add(typeColumn);
        view.getColumns().add(partitionKeyColumn);
        view.getColumns().add(clusteringKeyColumn);
        view.getColumns().add(indexColumn);
        view.setEditable(true);
        view.setMinWidth(properties.getTableEditorWidth());
        view.setMaxWidth(properties.getTableEditorWidth());
        view.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        view.setOnContextMenuRequested(event -> {
            TableView.TableViewSelectionModel<TableRowEntry> selectionModel = view.getSelectionModel();
            if (!selectionModel.isEmpty() || rows.isEmpty()) {
                widgetContext.show(view, event.getScreenX(), event.getScreenY());
            }
        });
        view.setOnMouseClicked(event -> {
            if (widgetContext.isShowing()) {
                widgetContext.hide();
            }
            PickResult pickResult = event.getPickResult();
            Node source = pickResult.getIntersectedNode();

            // move up through the node hierarchy until a TableRowEntry or scene root is found
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }

            // clear selection on click anywhere but on a filled row
            if (source == null || (source instanceof TableRow && ((TableRow) source).isEmpty())) {
                view.getSelectionModel().clearSelection();
            }
        });
        return view;
    }

    private TableRowEntry buildDefaultRow() {
        return new TableRowEntry(UUID.randomUUID().toString(), DataType.Name.TEXT, false, false, false);
    }

    private TextArea buildTableArea() {
        TextArea textArea = new TextArea("new_table");
        textArea.setMaxHeight(Region.USE_PREF_SIZE);
        textArea.setMinHeight(Region.USE_PREF_SIZE);
        textArea.setPrefHeight(25);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(1);
        return textArea;
    }

    private String toRawString() {
        return "";
    }

    private void moveUp() {
        TableView.TableViewSelectionModel<TableRowEntry> selectionModel = tableView.getSelectionModel();
        int selected = selectionModel.selectedIndexProperty().get();
        TableRowEntry movingEntry = rows.remove(selected);
        int newIndex = selected - 1;
        rows.add(newIndex, movingEntry);
        selectionModel.select(newIndex);
    }

    private void moveDown() {
        TableView.TableViewSelectionModel<TableRowEntry> selectionModel = tableView.getSelectionModel();
        int selected = selectionModel.selectedIndexProperty().get();
        TableRowEntry movingEntry = rows.remove(selected);
        int newIndex = selected + 1;
        if (newIndex == rows.size()) {
            rows.add(movingEntry);
        } else {
            rows.add(newIndex, movingEntry);
        }
        selectionModel.select(newIndex);
    }

}
