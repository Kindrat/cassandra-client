package com.github.kindrat.cassandra.client.ui.window.menu.file

import com.datastax.oss.driver.api.core.type.codec.TypeCodecs
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.properties.StorageProperties
import com.github.kindrat.cassandra.client.properties.UIProperties
import com.github.kindrat.cassandra.client.ui.ConnectionData
import com.github.kindrat.cassandra.client.ui.MainController
import com.github.kindrat.cassandra.client.ui.fx.CellFactory.create
import com.github.kindrat.cassandra.client.ui.window.menu.ConnectionDataHandler
import com.github.kindrat.cassandra.client.ui.window.menu.KeySpaceProvider
import com.github.kindrat.cassandra.client.util.JsonUtil.fromJson
import com.github.kindrat.cassandra.client.util.JsonUtil.toJson
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption.WRITE
import java.util.function.Consumer
import java.util.function.Function

private val logger = KotlinLogging.logger {}

class ConnectionManager(
        parent: Stage,
        private val localeService: MessageByLocaleService,
        private val keySpaceProvider: KeySpaceProvider,
        private val uiProperties: UIProperties,
        private val storageProperties: StorageProperties
) : Stage(), BeanFactoryAware {

    private val table: TableView<ConnectionData>
    private val configurationsFile: File
    private var beanFactory: BeanFactory? = null

    @Throws(BeansException::class)
    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    private fun getConfigurationsFile(): File {
        return File(storageProperties.location, storageProperties.propertiesFile)
    }

    private fun getButton(text: String): Button {
        val button = Button(text)
        button.maxWidth = uiProperties.connectionManagerButtonWidth
        button.minWidth = uiProperties.connectionManagerButtonWidth
        return button
    }

    private fun buildConnectionDataTable(leftWidth: Int): TableView<ConnectionData> {
        val dataTableView = TableView<ConnectionData>()
        dataTableView.minWidth = leftWidth.toDouble()
        dataTableView.maxWidth = leftWidth.toDouble()
        dataTableView.columnResizePolicy = TableView.UNCONSTRAINED_RESIZE_POLICY
        dataTableView.selectionModel.selectionMode = SelectionMode.SINGLE
        dataTableView.selectionModel.isCellSelectionEnabled = false
        dataTableView.onMouseClicked = EventHandler { mouseEvent: MouseEvent ->
            if (mouseEvent.clickCount > 1) {
                onConnectionUsage()
            }
        }
        val colWidth = leftWidth / 4
        val urlTitle = localeService.getMessage("ui.menu.file.manager.table.url")
        val url = buildColumn(urlTitle, colWidth) { data: ConnectionData -> data.url }
        val spaceTitle = localeService.getMessage("ui.menu.file.manager.table.keyspace")
        val keyspace = buildColumn(spaceTitle, colWidth) { data: ConnectionData -> data.keyspace }
        val dcTitle = localeService.getMessage("ui.menu.file.manager.table.dc")
        val dc = buildColumn(dcTitle, colWidth) { data: ConnectionData -> data.localDatacenter }
        val userTitle = localeService.getMessage("ui.menu.file.manager.table.username")
        val username = buildColumn(userTitle, colWidth) { data: ConnectionData -> data.username }
        val passTitle = localeService.getMessage("ui.menu.file.manager.table.password")
        val password = buildColumn(passTitle, colWidth) { data: ConnectionData -> data.password }
        dataTableView.columns.add(url)
        dataTableView.columns.add(keyspace)
        dataTableView.columns.add(dc)
        dataTableView.columns.add(username)
        dataTableView.columns.add(password)
        dataTableView.items = FXCollections.observableArrayList()
        return dataTableView
    }

    private fun buildColumn(name: String, columnWidth: Int, extractor: Function<ConnectionData, String?>): TableColumn<ConnectionData, String> {
        val column = TableColumn<ConnectionData, String>()
        val columnLabel = Label(name)
        column.cellFactory = create(TypeCodecs.TEXT)
        column.setCellValueFactory { param: TableColumn.CellDataFeatures<ConnectionData, String> ->
            val field = extractor.apply(param.value)
            SimpleObjectProperty(field)
        }
        column.setOnEditCommit { logger.info("edit") }
        column.graphic = columnLabel
        column.minWidth = columnWidth.toDouble()
        return column
    }

    private fun onNewConnection() {
        val handler = ConnectionDataHandler { handleConnectionData(it) }
        beanFactory!!.getBean("newConnectionBox", this, handler, keySpaceProvider)
    }

    private fun handleConnectionData(data: ConnectionData) {
        table.items.add(data)
        flushConfigFile(configurationsFile)
    }

    private fun onConnectionEdit() {
        val selectionModel = table.selectionModel
        if (!selectionModel.isEmpty) {
            val focusedIndex = selectionModel.focusedIndex
            val data = table.items[focusedIndex]
            val handler = ConnectionDataHandler { onConnectionEditData(it) }
            val connectionBox = beanFactory!!.getBean("newConnectionBox", this, handler, keySpaceProvider) as NewConnectionBox
            connectionBox.update(data)
        }
    }

    private fun onConnectionEditData(newData: ConnectionData) {
        val selectionModel = table.selectionModel
        if (!selectionModel.isEmpty) {
            val focusedIndex = selectionModel.focusedIndex
            val data = table.items[focusedIndex]
            val updatedData = data.copy(
                    url = newData.url,
                    keyspace = newData.keyspace,
                    localDatacenter = newData.localDatacenter,
                    username = newData.username,
                    password = newData.password
            )
            table.items[focusedIndex] = updatedData
            // hack: better way to refresh the display??
            table.columns[0].isVisible = false
            table.columns[0].isVisible = true
        }
        flushConfigFile(configurationsFile)
    }

    private fun onConnectionUsage() {
        doWithSelected { connectionData: ConnectionData ->
            val controller = beanFactory!!.getBean(MainController::class.java)
            controller.loadTables(connectionData)
            close()
        }
    }

    private fun onConnectionRemove() {
        doWithSelected { connectionData: ConnectionData ->
            table.items.remove(connectionData)
            flushConfigFile(configurationsFile)
        }
    }

    private fun doWithSelected(callback: Consumer<ConnectionData>) {
        val selectionModel = table.selectionModel
        if (!selectionModel.isEmpty) {
            val focusedIndex = selectionModel.focusedIndex
            val connectionData = table.items[focusedIndex]
            callback.accept(connectionData)
        }
    }

    private fun flushConfigFile(configurationsFile: File) {
        Files.write(configurationsFile.toPath(), toJson(table.items).toByteArray(StandardCharsets.UTF_8), WRITE)
    }

    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parent)
        title = localeService.getMessage("ui.menu.file.manager.title")
        icons.add(Image("cassandra_ico.png"))
        val width = uiProperties.connectionManagerWidth.toInt()
        val height = uiProperties.connectionManagerHeight.toInt()
        val rightWidth = uiProperties.connectionManagerRightPaneWidth.toInt()
        val leftWidth = width - rightWidth
        val connectionBox = VBox(uiProperties.newConnectSpacing)
        connectionBox.alignment = Pos.CENTER
        connectionBox.minWidth = leftWidth.toDouble()
        connectionBox.maxWidth = leftWidth.toDouble()
        table = buildConnectionDataTable(leftWidth)
        configurationsFile = getConfigurationsFile()
        if (!configurationsFile.exists()) {
            configurationsFile.createNewFile()
        }
        val json = Files.readString(configurationsFile.toPath())
        if (!StringUtils.isBlank(json)) {
            val connections = fromJson(json, ConnectionData::class.java)
            table.items.addAll(connections)
        }
        connectionBox.children.add(table)
        val buttons = VBox()
        buttons.minWidth = rightWidth.toDouble()
        buttons.maxWidth = rightWidth.toDouble()
        buttons.spacing = uiProperties.connectionManagerButtonsSpacing
        buttons.alignment = Pos.CENTER
        val newConnection = getButton("+")
        newConnection.onAction = EventHandler { onNewConnection() }
        val removeConnection = getButton("-")
        removeConnection.onAction = EventHandler { onConnectionRemove() }
        val editConnection = getButton("✎")
        editConnection.onAction = EventHandler { onConnectionEdit() }
        val useConnection = getButton("✓")
        useConnection.onAction = EventHandler { onConnectionUsage() }
        buttons.children.add(newConnection)
        buttons.children.add(removeConnection)
        buttons.children.add(editConnection)
        buttons.children.add(useConnection)
        val splitPane = SplitPane(connectionBox, buttons)
        val content = Scene(splitPane, width.toDouble(), height.toDouble())
        scene = content
        show()
    }
}