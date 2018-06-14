package com.github.kindrat.cassandra.client.ui.window.menu.file;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import com.github.kindrat.cassandra.client.ui.ConnectionData;
import com.github.kindrat.cassandra.client.ui.eventhandler.TextFieldButtonWatcher;
import com.github.kindrat.cassandra.client.ui.window.menu.ConnectionDataHandler;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class NewConnectionBox extends Stage {
    private final MessageByLocaleService localeService;
    private final UIProperties uiProperties;
    private final ConnectionDataHandler valueHandler;
    private final TextField urlField;
    private final TextField keyspaceField;
    private final AuthCredentialsBox credentials;
    private final CheckBox authTriggerBox;
    private final ObservableList<Node> children;

    public NewConnectionBox(Stage parent, MessageByLocaleService localeService, UIProperties uiProperties,
            ConnectionDataHandler valueHandler) {
        this.localeService = localeService;
        this.uiProperties = uiProperties;
        this.valueHandler = valueHandler;

        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setTitle(localeService.getMessage("ui.menu.file.connect.title"));

        VBox connectBox = new VBox(uiProperties.getNewConnectSpacing());
        connectBox.setAlignment(Pos.CENTER);
        Scene content = new Scene(connectBox, uiProperties.getNewConnectWidth(), uiProperties.getNewConnectHeight());
        children = connectBox.getChildren();
        urlField = getUrlField(uiProperties.getNewConnectWidth());
        urlField.setText("localhost:9042");
        children.add(urlField);
        keyspaceField = getKeyspaceField(uiProperties.getNewConnectWidth());
        children.add(keyspaceField);
        authTriggerBox = getAuthTriggerBox();
        children.add(authTriggerBox);

        credentials = new AuthCredentialsBox(localeService, uiProperties);
        credentials.setMinWidth(uiProperties.getNewConnectWidth() - 10);
        credentials.setMaxWidth(uiProperties.getNewConnectWidth() - 10);

        Button submitButton = buildButton();
        children.add(submitButton);

        urlField.textProperty().addListener(TextFieldButtonWatcher.wrap(submitButton));
        keyspaceField.textProperty().addListener(TextFieldButtonWatcher.wrap(submitButton));

        urlField.requestFocus();
        setScene(content);
        show();
    }

    public void update(ConnectionData data) {
        urlField.setText(data.getUrl());
        keyspaceField.setText(data.getKeyspace());
        if (isNotBlank(data.getUsername()) || isNotBlank(data.getPassword())) {
            authTriggerBox.setSelected(true);
            onAuthTrigger(null);
            credentials.setUsername(data.getUsername());
            credentials.setPassword(data.getPassword());
        }
    }

    private Button buildButton() {
        Button submit = new Button(localeService.getMessage("ui.menu.file.connect.submit.text"));
        submit.setAlignment(Pos.CENTER);
        submit.setOnAction(this::handleClick);
        submit.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleClick(event);
            }
        });
        return submit;
    }

    private TextField getKeyspaceField(int width) {
        TextField keyspace = new TextField();
        keyspace.setPromptText(localeService.getMessage("ui.menu.file.connect.keyspace.text"));
        keyspace.setAlignment(Pos.TOP_CENTER);
        keyspace.setMinWidth(width - 10);
        keyspace.setMaxWidth(width - 10);
        keyspace.setOnAction(this::handleClick);
        return keyspace;
    }

    private TextField getUrlField(int width) {
        TextField url = new TextField();
        url.setPromptText(localeService.getMessage("ui.menu.file.connect.url.text"));
        url.setAlignment(Pos.CENTER);
        url.setMinWidth(width - 10);
        url.setMaxWidth(width - 10);
        return url;
    }

    private CheckBox getAuthTriggerBox() {
        CheckBox checkBox = new CheckBox(localeService.getMessage("ui.menu.file.connect.auth.checkbox"));
        checkBox.setSelected(false);
        checkBox.addEventHandler(ActionEvent.ACTION, this::onAuthTrigger);
        return checkBox;
    }

    @SuppressWarnings("unused")
    private void handleClick(Event event) {
        String url = urlField.getText();
        String keyspace = keyspaceField.getText();

        if (StringUtils.isNoneBlank(url, keyspace)) {
            valueHandler.onConnectionData(new ConnectionData(url, keyspace,
                    credentials.getUsername(), credentials.getPassword()));
            close();
        }
    }

    @SuppressWarnings("unused")
    private void onAuthTrigger(ActionEvent event) {
        boolean shouldShow = authTriggerBox.isSelected();
        if (shouldShow) {
            children.add(2, credentials);
            credentials.setVisible(!credentials.isVisible());
            setHeight(getHeight() + uiProperties.getCredentialsBoxHeight());
        } else {
            credentials.setVisible(false);
            children.remove(2);
            setHeight(getHeight() - uiProperties.getCredentialsBoxHeight());
        }
    }
}
