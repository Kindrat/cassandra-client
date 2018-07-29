package com.github.kindrat.cassandra.client.ui.window.editor.tables;

import com.datastax.driver.core.DataType;
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import com.github.kindrat.cassandra.client.ui.ToggleSwitch;
import com.github.kindrat.cassandra.client.util.UIUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.github.kindrat.cassandra.client.util.UIUtil.fillParent;

@Slf4j
public class TableEditWidget extends Stage {
    private final MessageByLocaleService localeService;
    private final UIProperties properties;
    private final Pane editorHolder = new Pane();

    private final ObservableList<TableRow> rows = FXCollections.observableArrayList();
    private final TableView<TableRow> tableEditor;
    private final TextArea textView = new TextArea();

    public TableEditWidget(Stage parent, MessageByLocaleService localeService, UIProperties properties) {
        this.localeService = localeService;
        this.properties = properties;
        this.tableEditor = buildTableView();
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

        ObservableList<Node> children = editor.getChildren();
        children.add(sliderBox);
        children.add(editorHolder);
        children.add(buttons);

        dialogScene.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            tableEditor.setPrefHeight(newValue.doubleValue() - sliderBox.getHeight() - buttons.getHeight());
            textView.setPrefHeight(newValue.doubleValue() - sliderBox.getHeight() - buttons.getHeight());
        });
        dialogScene.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            UIUtil.setWidth(editorHolder, newValue);
            UIUtil.setWidth(tableEditor, newValue);
            UIUtil.setWidth(textView, newValue);
            UIUtil.setWidth(buttons, newValue);
        });

        tableEditor.setPrefWidth(editor.getPrefWidth());
        tableEditor.setPrefHeight(editor.getPrefWidth() - sliderBox.getHeight() - buttons.getHeight());
        textView.setPrefWidth(editor.getPrefWidth());
        textView.setPrefHeight(editor.getPrefWidth() - sliderBox.getHeight() - buttons.getHeight());

        editorHolder.getChildren().add(tableEditor);
        return dialogScene;
    }

    private TableView<TableRow> buildTableView() {
        TableView<TableRow> view = new TableView<>(rows);
        view.setMinWidth(properties.getTableEditorWidth());
        view.setMaxWidth(properties.getTableEditorWidth());
        view.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        return view;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TableRow {
        private String name;
        private DataType.Name type;
    }
}
