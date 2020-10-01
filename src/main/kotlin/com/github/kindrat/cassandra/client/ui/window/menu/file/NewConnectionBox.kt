package com.github.kindrat.cassandra.client.ui.window.menu.file

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.properties.UIProperties
import com.github.kindrat.cassandra.client.ui.ConnectionData
import com.github.kindrat.cassandra.client.ui.fx.component.LabeledComponentColumn
import com.github.kindrat.cassandra.client.ui.listener.TextFieldButtonWatcher.Companion.wrap
import com.github.kindrat.cassandra.client.ui.window.menu.ConnectionDataHandler
import com.github.kindrat.cassandra.client.ui.window.menu.KeySpaceProvider
import com.github.kindrat.cassandra.client.util.UIUtil.disable
import com.github.kindrat.cassandra.client.util.UIUtil.enable
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.data.cassandra.config.CompressionType

private val logger = KotlinLogging.logger {}

class NewConnectionBox(
        parent: Stage,
        private val localeService: MessageByLocaleService,
        private val uiProperties: UIProperties,
        private val valueHandler: ConnectionDataHandler,
        private val keyspaceProvider: KeySpaceProvider
) : Stage() {

    private val keyspaces = FXCollections.observableArrayList<String>()
    private val urlField: TextField
    private val dcField: TextField
    private val keyspaceField: ComboBox<String>
    private val credentials: AuthCredentialsBox
    private val authTriggerBox: CheckBox
    private val children: ObservableList<Node>
    private val submitButton: Button

    fun update(data: ConnectionData) {
        urlField.text = data.url
        keyspaces.add(data.keyspace)
        if (StringUtils.isNotBlank(data.username) || StringUtils.isNotBlank(data.password)) {
            authTriggerBox.isSelected = true
            onAuthTrigger()
            credentials.setUsername(data.username)
            credentials.setPassword(data.password)
        }
        enable(submitButton)
    }

    private fun buildButton(): Button {
        val submit = Button(localeService.getMessage("ui.menu.file.connect.submit.text"))
        submit.alignment = Pos.CENTER
        submit.onAction = EventHandler { handleClick() }
        submit.onKeyPressed = EventHandler { event: KeyEvent ->
            if (event.code == KeyCode.ENTER) {
                handleClick()
            }
        }
        return submit
    }

    private fun getKeyspaceField(width: Int): ComboBox<String> {
        val keyspace = ComboBox(keyspaces)
        keyspace.promptText = localeService.getMessage("ui.menu.file.connect.keyspace.load_text")
        keyspace.minWidth = width - 20.toDouble()
        keyspace.maxWidth = width - 20.toDouble()
        keyspace.onAction = EventHandler { handleClick() }
        return keyspace
    }

    private fun getUrlField(width: Int): TextField {
        val url = TextField()
        url.promptText = localeService.getMessage("ui.menu.file.connect.url.text")
//        url.alignment = Pos.CENTER
//        url.minWidth = width - 10.toDouble()
//        url.maxWidth = width - 10.toDouble()
        return url
    }

    private fun getAuthTriggerBox(): CheckBox {
        val checkBox = CheckBox(localeService.getMessage("ui.menu.file.connect.auth.checkbox"))
        checkBox.isSelected = false
        checkBox.addEventHandler(ActionEvent.ACTION) { onAuthTrigger() }
        return checkBox
    }

    private fun handleClick() {
        val url = urlField.text
        val keyspace = keyspaceField.selectionModel.selectedItem
        if (StringUtils.isNoneBlank(url, keyspace)) {
            valueHandler.onConnectionData(ConnectionData(url, keyspace, dcField.text,
                    credentials.getUsername(), credentials.getPassword(), CompressionType.NONE))
            close()
        }
    }

    private fun onAuthTrigger() {
        val shouldShow = authTriggerBox.isSelected
        if (shouldShow) {
            children.add(2, credentials)
            credentials.isVisible = !credentials.isVisible
            height += uiProperties.credentialsBoxHeight
        } else {
            credentials.isVisible = false
            children.removeAt(2)
            height -= uiProperties.credentialsBoxHeight
        }
    }

    private fun loadKeyspaces() {
        keyspaces.clear()
        Platform.runLater { keyspaceField.setPromptText(localeService.getMessage("ui.menu.file.connect.keyspace.text")) }
        keyspaceProvider.loadKeyspaces(urlField.text, dcField.text, credentials.getUsername(), credentials.getPassword())
                .whenComplete { strings: List<String?>?, throwable: Throwable? ->
                    if (throwable != null) {
                        logger.error("Could not load keyspaces : ${throwable.message}")
                        Platform.runLater {
                            val message = localeService.getMessage("ui.menu.file.connect.keyspace.error_text")
                            keyspaceField.setPromptText(message)
                        }
                    } else {
                        logger.debug("Keyspaces loaded for ${urlField.text}")
                        keyspaces.addAll(strings!!)
                    }
                }
    }

    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parent)
        title = localeService.getMessage("ui.menu.file.connect.title")
        icons.add(Image("cassandra_ico.png"))
        val connectBox = VBox(uiProperties.newConnectSpacing)
        connectBox.alignment = Pos.CENTER
        val content = Scene(connectBox, uiProperties.newConnectWidth, uiProperties.newConnectHeight)
        children = connectBox.children
        urlField = getUrlField(uiProperties.newConnectWidth.toInt())
        urlField.text = "localhost:9042"
        dcField = TextField(uiProperties.defaultDatacenter)
        val connectionProps = LabeledComponentColumn(1.0)
        connectionProps.addLabeledElement("URL", urlField)
        connectionProps.addLabeledElement(localeService.getMessage("ui.menu.file.connect.dc.label"), dcField)
//        children.add(urlField)
//        dcField.alignment = Pos.CENTER
        children.add(connectionProps)
        keyspaceField = getKeyspaceField(uiProperties.newConnectWidth.toInt())
        val keyspaceLoaderRefreshBtn = Button("↺")
        keyspaceLoaderRefreshBtn.onAction = EventHandler { loadKeyspaces() }
        val keyspaceBox = HBox(keyspaceField, keyspaceLoaderRefreshBtn)
        keyspaceBox.alignment = Pos.CENTER
        children.add(keyspaceBox)
        authTriggerBox = getAuthTriggerBox()
        children.add(authTriggerBox)
        credentials = AuthCredentialsBox(localeService, uiProperties)
        credentials.minWidth = uiProperties.newConnectWidth - 10
        credentials.maxWidth = uiProperties.newConnectWidth - 10
        submitButton = buildButton()
        children.add(submitButton)
        disable(submitButton)
        urlField.textProperty().addListener(wrap(submitButton))
        urlField.textProperty().addListener { _: ObservableValue<out String?>?, oldValue: String?, newValue: String? ->
            if (!StringUtils.equals(oldValue, newValue) && !StringUtils.isBlank(newValue)) {
                loadKeyspaces()
            }
        }
        keyspaceField.onAction = EventHandler {
            if (keyspaceField.selectionModel.selectedIndex != -1) {
                enable(submitButton)
            } else {
                disable(submitButton)
            }
        }
        width = 300.0
        urlField.requestFocus()
        scene = content
        show()
    }
}