package com.github.kindrat.cassandra.client.ui.menu.file;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.BiConsumer;

public class NewConnectionBox extends Stage {
    private final MessageByLocaleService localeService;
    private final BiConsumer<String, String> valueHandler;
    private final TextField urlField;
    private final TextField keyspaceField;

    public NewConnectionBox(Stage parent, MessageByLocaleService localeService, UIProperties uiProperties,
                            BiConsumer<String, String> valueHandler) {
        this.localeService = localeService;
        this.valueHandler = valueHandler;

        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setTitle(localeService.getMessage("ui.menu.file.connect.title"));

        VBox connectBox = new VBox(uiProperties.getNewConnectSpacing());
        connectBox.setAlignment(Pos.CENTER);
        Scene content = new Scene(connectBox, uiProperties.getNewConnectWidth(), uiProperties.getNewConnectHeight());
        ObservableList<Node> children = connectBox.getChildren();
        urlField = getUrlField(uiProperties.getNewConnectWidth());
        children.add(urlField);
        keyspaceField = getKeyspaceField(uiProperties.getNewConnectWidth());
        children.add(keyspaceField);
        children.add(buildButton());
        buildButton().requestFocus();

        setScene(content);
        show();
    }

    private Button buildButton() {
        Button submit = new Button(localeService.getMessage("ui.menu.file.connect.submit.text"));
        submit.setAlignment(Pos.CENTER);
        submit.setOnAction(this::handleClick);
        submit.setOnKeyPressed(this::handleClick);
        return submit;
    }

    private TextField getKeyspaceField(int width) {
        TextField keyspace = new TextField();
        keyspace.setPromptText(localeService.getMessage("ui.menu.file.connect.keyspace.text"));
        keyspace.setAlignment(Pos.TOP_CENTER);
        keyspace.setTooltip(new Tooltip(localeService.getMessage("ui.menu.file.connect.keyspace.tooltip")));
        keyspace.setMinWidth(width - 10);
        keyspace.setMaxWidth(width - 10);
        keyspace.setOnAction(this::handleClick);
        return keyspace;
    }

    private TextField getUrlField(int width) {
        TextField url = new TextField();
        url.setPromptText(localeService.getMessage("ui.menu.file.connect.url.text"));
        url.setTooltip(new Tooltip(localeService.getMessage("ui.menu.file.connect.url.tooltip")));
        url.setAlignment(Pos.CENTER);
        url.setMinWidth(width - 10);
        url.setMaxWidth(width - 10);
        return url;
    }

    private void handleClick(Event event) {
        valueHandler.accept(urlField.getText(), keyspaceField.getText());
        close();
    }
}
