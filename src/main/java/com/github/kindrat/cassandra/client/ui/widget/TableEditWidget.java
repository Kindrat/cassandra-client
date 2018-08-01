package com.github.kindrat.cassandra.client.ui.widget;

import com.datastax.driver.core.DataType;
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import com.github.kindrat.cassandra.client.ui.fx.CellValueFactory;
import com.github.kindrat.cassandra.client.ui.fx.component.ToggleSwitch;
import com.github.kindrat.cassandra.client.util.UIUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.github.kindrat.cassandra.client.ui.fx.TableColumns.buildColumn;
import static com.github.kindrat.cassandra.client.util.UIUtil.computeTextContainerWidth;
import static com.github.kindrat.cassandra.client.util.UIUtil.fillParent;

@Slf4j
public class TableEditWidget extends Stage {
    private final MessageByLocaleService localeService;
    private final UIProperties properties;
    private final Pane editorHolder = new Pane();

    private final ObservableList<TableRow> rows = FXCollections.observableArrayList();
    private final TextArea tableNameArea;
    private final TableView<TableRow> tableEditor;
    private final TextArea textView = new TextArea();

    public TableEditWidget(Stage parent, MessageByLocaleService localeService, UIProperties properties) {
        this.localeService = localeService;
        this.properties = properties;
        this.tableEditor = buildTableView();
        this.tableNameArea = buildTableArea();
        setTitle(localeService.getMessage("ui.editor.table_editor.title"));
        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setScene(buildScene());
        show();
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
            editorHolder.getChildren().add(textView);
        });

        toggleSwitch.onDisabled(() -> {
            editorHolder.getChildren().clear();
            editorHolder.getChildren().add(tableEditor);
        });

        Button createButton = new Button(localeService.getMessage("ui.editor.table_editor.buttons.create"));
        Button resetButton = new Button(localeService.getMessage("ui.editor.table_editor.buttons.reset"));
        HBox buttons = new HBox(20.0, createButton, resetButton);
        buttons.setAlignment(Pos.CENTER);

        VBox headerBox = new VBox(sliderBox, tableNameBox);
        ObservableList<Node> children = editor.getChildren();
        children.add(headerBox);
        children.add(editorHolder);
        children.add(buttons);

        dialogScene.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            tableEditor.setPrefHeight(newValue.doubleValue() - headerBox.getHeight() - buttons.getHeight());
            textView.setPrefHeight(newValue.doubleValue() - headerBox.getHeight() - buttons.getHeight());
        });
        dialogScene.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            UIUtil.setWidth(editorHolder, newValue);
            UIUtil.setWidth(tableEditor, newValue);
            UIUtil.setWidth(textView, newValue);
            UIUtil.setWidth(buttons, newValue);
        });

        tableEditor.setPrefWidth(editor.getPrefWidth());
        tableEditor.setPrefHeight(editor.getPrefWidth() - headerBox.getHeight() - buttons.getHeight());
        textView.setPrefWidth(editor.getPrefWidth());
        textView.setPrefHeight(editor.getPrefWidth() - headerBox.getHeight() - buttons.getHeight());

        editorHolder.getChildren().add(tableEditor);
        return dialogScene;
    }

    private TableView<TableRow> buildTableView() {
        rows.add(new TableRow("changeme", DataType.Name.TEXT, false, false, false));
        TableView<TableRow> view = new TableView<>(rows);

        TableColumn<TableRow, Object> nameColumn = buildColumn(DataType.text(), "Name");
        nameColumn.setCellValueFactory(CellValueFactory.create(TableRow::getName));
        TableColumn<TableRow, DataType.Name> typeColumn = buildColumn(DataType.Name.class, "Type");
        typeColumn.setCellValueFactory(CellValueFactory.create(TableRow::getType));
        TableColumn<TableRow, Object> partitionKeyColumn = new TableColumn<>("Partition Key");
        partitionKeyColumn.setCellValueFactory(CellValueFactory.create(TableRow::getIsPartitionKey));
        TableColumn<TableRow, Object> clusteringKeyColumn = new TableColumn<>("Clustering Key");
        clusteringKeyColumn.setCellValueFactory(CellValueFactory.create(TableRow::getIsClusteringKey));
        TableColumn<TableRow, Object> indexColumn = new TableColumn<>("Index");
        indexColumn.setCellValueFactory(CellValueFactory.create(TableRow::getHasIndex));

        view.getColumns().add(nameColumn);
        view.getColumns().add(typeColumn);
        view.getColumns().add(partitionKeyColumn);
        view.getColumns().add(clusteringKeyColumn);
        view.getColumns().add(indexColumn);
        view.setEditable(true);
        view.setMinWidth(properties.getTableEditorWidth());
        view.setMaxWidth(properties.getTableEditorWidth());
        view.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        return view;
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TableRow {
        private String name;
        private DataType.Name type;
        private Boolean isPartitionKey;
        private Boolean isClusteringKey;
        private Boolean hasIndex;
    }
}
