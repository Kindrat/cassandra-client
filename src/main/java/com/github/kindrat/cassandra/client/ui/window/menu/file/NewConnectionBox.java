package com.github.kindrat.cassandra.client.ui.window.menu.file;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import com.github.kindrat.cassandra.client.ui.ConnectionData;
import com.github.kindrat.cassandra.client.ui.listener.TextFieldButtonWatcher;
import com.github.kindrat.cassandra.client.ui.window.menu.ConnectionDataHandler;
import com.github.kindrat.cassandra.client.ui.window.menu.KeySpaceProvider;
import com.github.kindrat.cassandra.client.util.UIUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.github.kindrat.cassandra.client.util.UIUtil.disable;
import static com.github.kindrat.cassandra.client.util.UIUtil.enable;
import static javafx.application.Platform.runLater;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class NewConnectionBox extends Stage {
    private final ObservableList<String> keyspaces = FXCollections.observableArrayList();
    private final MessageByLocaleService localeService;
    private final UIProperties uiProperties;
    private final ConnectionDataHandler valueHandler;
    private final KeySpaceProvider keyspaceProvider;
    private final TextField urlField;
    private final ComboBox<String> keyspaceField;
    private final AuthCredentialsBox credentials;
    private final CheckBox authTriggerBox;
    private final ObservableList<Node> children;
    private final Button submitButton;

    public NewConnectionBox(Stage parent, MessageByLocaleService localeService, UIProperties uiProperties,
            ConnectionDataHandler valueHandler, KeySpaceProvider keyspaceProvider) {
        this.localeService = localeService;
        this.uiProperties = uiProperties;
        this.valueHandler = valueHandler;
        this.keyspaceProvider = keyspaceProvider;

        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setTitle(localeService.getMessage("ui.menu.file.connect.title"));
        getIcons().add(new Image("cassandra_ico.png"));

        VBox connectBox = new VBox(uiProperties.getNewConnectSpacing());
        connectBox.setAlignment(Pos.CENTER);
        Scene content = new Scene(connectBox, uiProperties.getNewConnectWidth(), uiProperties.getNewConnectHeight());
        children = connectBox.getChildren();
        urlField = getUrlField(uiProperties.getNewConnectWidth());
        urlField.setText("localhost:9042");
        children.add(urlField);

        keyspaceField = getKeyspaceField(uiProperties.getNewConnectWidth());
        Button keyspaceLoaderRefreshBtn = new Button("↺");
        keyspaceLoaderRefreshBtn.setOnAction(actionEvent -> loadKeyspaces());
        HBox keyspaceBox = new HBox(keyspaceField, keyspaceLoaderRefreshBtn);
        keyspaceBox.setAlignment(Pos.CENTER);

        children.add(keyspaceBox);
        authTriggerBox = getAuthTriggerBox();
        children.add(authTriggerBox);

        credentials = new AuthCredentialsBox(localeService, uiProperties);
        credentials.setMinWidth(uiProperties.getNewConnectWidth() - 10);
        credentials.setMaxWidth(uiProperties.getNewConnectWidth() - 10);

        submitButton = buildButton();
        children.add(submitButton);
        UIUtil.disable(submitButton);
        urlField.textProperty().addListener(TextFieldButtonWatcher.wrap(submitButton));
        urlField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!StringUtils.equals(oldValue, newValue) && !StringUtils.isBlank(newValue)) {
                loadKeyspaces();
            }
        });
        keyspaceField.setOnAction(actionEvent -> {
            if (keyspaceField.getSelectionModel().getSelectedIndex() != -1) {
                enable(submitButton);
            } else {
                disable(submitButton);
            }

        });

        setWidth(300);
        urlField.requestFocus();
        setScene(content);
        show();
    }

    public void update(ConnectionData data) {
        urlField.setText(data.getUrl());
        keyspaces.add(data.getKeyspace());
        if (isNotBlank(data.getUsername()) || isNotBlank(data.getPassword())) {
            authTriggerBox.setSelected(true);
            onAuthTrigger(null);
            credentials.setUsername(data.getUsername());
            credentials.setPassword(data.getPassword());
        }
        enable(submitButton);
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

    private ComboBox<String> getKeyspaceField(int width) {
        ComboBox<String> keyspace = new ComboBox<>(keyspaces);
        keyspace.setPromptText(localeService.getMessage("ui.menu.file.connect.keyspace.load_text"));
        keyspace.setMinWidth(width - 20);
        keyspace.setMaxWidth(width - 20);
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
        String keyspace = keyspaceField.getSelectionModel().getSelectedItem();

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

    private void loadKeyspaces() {
        keyspaces.clear();
        runLater(() -> keyspaceField.setPromptText(localeService.getMessage("ui.menu.file.connect.keyspace.text")));
        keyspaceProvider.loadKeyspaces(urlField.getText(), credentials.getUsername(), credentials.getPassword())
                .whenComplete((strings, throwable) -> {
                    if (throwable != null) {
                        log.error("Could not load keyspaces : {}", throwable.getMessage());
                        runLater(() -> {
                            String message = localeService.getMessage("ui.menu.file.connect.keyspace.error_text");
                            keyspaceField.setPromptText(message);
                        });
                    } else {
                        log.debug("Keyspaces loaded for {}", urlField.getText());
                        keyspaces.addAll(strings);
                    }
                });
    }
}
