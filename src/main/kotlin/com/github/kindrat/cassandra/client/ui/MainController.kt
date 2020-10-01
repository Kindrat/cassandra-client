package com.github.kindrat.cassandra.client.ui

import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.properties.StorageProperties
import com.github.kindrat.cassandra.client.properties.UIProperties
import com.github.kindrat.cassandra.client.service.BackgroundTaskExecutor
import com.github.kindrat.cassandra.client.service.CassandraClientAdapter
import com.github.kindrat.cassandra.client.service.TableContext
import com.github.kindrat.cassandra.client.service.TableContext.Companion.customView
import com.github.kindrat.cassandra.client.service.TableContext.Companion.fullTable
import com.github.kindrat.cassandra.client.ui.event.StageInitializedEvent
import com.github.kindrat.cassandra.client.ui.listener.TextFieldButtonWatcher.Companion.wrap
import com.github.kindrat.cassandra.client.ui.widget.DataExportWidget
import com.github.kindrat.cassandra.client.ui.window.editor.main.BackgroundTaskMonitor
import com.github.kindrat.cassandra.client.ui.window.editor.main.EventLogger
import com.github.kindrat.cassandra.client.ui.window.editor.main.TableDataGridPane
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.DdlTextArea
import com.github.kindrat.cassandra.client.ui.window.editor.tables.TablePanel
import com.github.kindrat.cassandra.client.ui.window.menu.ConnectionDataHandler
import com.github.kindrat.cassandra.client.ui.window.menu.KeySpaceProvider
import com.github.kindrat.cassandra.client.ui.window.menu.about.AboutBox
import com.github.kindrat.cassandra.client.ui.window.menu.file.ConnectionManager
import com.github.kindrat.cassandra.client.ui.window.menu.file.FileMenu
import com.github.kindrat.cassandra.client.ui.window.menu.file.NewConnectionBox
import com.github.kindrat.cassandra.client.util.CqlUtil.getSelectTable
import com.github.kindrat.cassandra.client.util.CqlUtil.isSelect
import com.github.kindrat.cassandra.client.util.EvenMoreFutures.handleErrorIfPresent
import com.github.kindrat.cassandra.client.util.UIUtil.disable
import com.github.kindrat.cassandra.client.util.UIUtil.enable
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.stage.Stage
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener

private val logger = KotlinLogging.logger {}

class MainController : ApplicationListener<StageInitializedEvent> {
    private val pageSize = 1000

    @Autowired
    private lateinit var eventLogger: EventLogger

    @Autowired
    private lateinit var clientAdapter: CassandraClientAdapter

    @Autowired
    private lateinit var localeService: MessageByLocaleService

    @Autowired
    private lateinit var tablePanel: TablePanel

    @Autowired
    private lateinit var backgroundTaskExecutor: BackgroundTaskExecutor

    @Autowired
    private lateinit var backgroundTaskMonitor: BackgroundTaskMonitor

    @Autowired
    private lateinit var tableDataGridPane: TableDataGridPane

    @Autowired
    private lateinit var uiProperties: UIProperties

    @Autowired
    private lateinit var storageProperties: StorageProperties

    @Autowired
    private lateinit var mainView: Parent

    @FXML
    private lateinit var menu: MenuBar

    @FXML
    private lateinit var mainWindow: SplitPane

    @FXML
    private lateinit var runButton: Button

    @FXML
    private lateinit var queryTextField: TextField

    @FXML
    private lateinit var editorAnchor: AnchorPane

    @FXML
    private lateinit var eventAnchor: AnchorPane

    private val tableMetadata: MutableMap<String, TableMetadata> = HashMap()
    private val ddlTextArea: DdlTextArea = DdlTextArea()

    fun showDDLForTable(tableName: String) {
        tableDataGridPane.isVisible = false
        ddlTextArea.showTableDDL(tableMetadata[tableName]!!)
    }

    fun showDataForTable(tableName: String) {
        eventLogger.fireLogEvent("Loading data for table '$tableName'")
        showDataRows(fullTable(tableName, tableMetadata[tableName]!!, clientAdapter, pageSize))
    }

    fun exportDataForTable(tableName: String) {
        eventLogger.fireLogEvent("Exporting data for table '$tableName'")
        // TODO
//        dataExporter(tableName).metadata
//                .map { CsvWriteBackgroundTask(it, tableMetadata!![tableName]!!, clientAdapter) }
//                .map { backgroundTaskExecutor.submit(it) }
//                .map { uuid: UUID ->
//                    backgroundTaskMonitor.addTask(uuid)
//                    Mono.just(uuid)
//                            .doOnNext { id: UUID -> backgroundTaskMonitor.addTask(id) }
//                            .doOnError { backgroundTaskMonitor.remove(uuid) }
//                            .doOnError { logger.error("Task $uuid failed", it) }
//                }
//                .doOnSuccess { eventLogger.fireLogEvent("Exported data for table '$tableName'") }
//                .subscribe()
    }

    fun loadTables(connection: ConnectionData) {
        tableDataGridPane.isVisible = false
        ddlTextArea.isVisible = false
        eventLogger.clear()
        eventLogger.fireLogEvent("Connecting to ${connection.url}/${connection.keyspace} ...")
        tablePanel.clear()
        disable(tablePanel, queryTextField, runButton)
        clientAdapter.connect(connection)
                .thenApply { it.keyspaceMetadata }
                .thenApply { it.tables }
                .thenApply { map -> map.entries.map { it.key.toString() to it.value }.toMap() }
                .whenComplete(handleErrorIfPresent { printError(it) })
                .whenComplete { metadata: Map<String, TableMetadata>?, _: Throwable? ->
                    if (metadata != null) {
                        tableMetadata.clear()
                        tableMetadata.putAll(metadata)
                        showTableNames(connection.url, connection.keyspace)
                    }
                }
    }

    override fun onApplicationEvent(event: StageInitializedEvent) {
        val mainStage = event.stage

        mainWindow.items.add(0, tablePanel)
        menu.menus.addAll(fileMenu(mainStage), helpMenu(mainStage))
        queryTextField.textProperty().addListener(wrap(runButton))
        queryTextField.promptText = localeService.getMessage("ui.editor.query.textbox.tooltip")
        runButton.tooltip = Tooltip(localeService.getMessage("ui.editor.query.button.tooltip"))
        runButton.onAction = EventHandler {
            val cqlQuery = queryTextField.text
            if (isSelect(cqlQuery)) {
                getSelectTable(cqlQuery)?.apply {
                    showDataRows(customView(this, cqlQuery, tableMetadata[this]!!, clientAdapter, pageSize))
                }
            } else {
                clientAdapter.execute(cqlQuery).whenComplete(handleErrorIfPresent { printError(it) })
            }
        }
        tablePanel.setNewValueListener { tableDataGridPane.updateTableMetadata(tableMetadata[it]) }
        editorAnchor.children.add(ddlTextArea)
        editorAnchor.children.add(tableDataGridPane)
        val eventBox = HBox(eventLogger, backgroundTaskMonitor)
        eventBox.alignment = Pos.BASELINE_CENTER
        eventAnchor.children.add(eventBox)
        eventLogger.prefWidthProperty().bind(eventAnchor.widthProperty().multiply(0.9))
        disable(queryTextField, runButton, tablePanel)

        accelerators[KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY)] = tableDataGridPane.buildCopyHandler()
        accelerators[KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_ANY)] = Runnable { tableDataGridPane.suggestCompletion() }
    }

    private val accelerators: ObservableMap<KeyCombination, Runnable>
        get() = mainView.scene.accelerators

    private fun showDataRows(context: TableContext) {
        Platform.runLater {
            ddlTextArea.isVisible = false
            tableDataGridPane.setDataColumns(context.columns)
            context.previousPage()
                    .whenComplete(handleErrorIfPresent { printError(it) })
                    ?.whenComplete { data: ObservableList<DataObject>?, _: Throwable? ->
                        if (data != null) {
                            tableDataGridPane.setData(context, data)
                        }
                    }
        }
    }

    private fun showTableNames(url: String, keyspace: String) {
        Platform.runLater {
            tablePanel.showTables(FXCollections.observableArrayList(tableMetadata.keys).sorted())
            eventLogger.printServerName(url, keyspace)
            eventLogger.fireLogEvent("Loaded tables from $url/$keyspace")
            enable(queryTextField, tablePanel)
        }
    }

    private fun printError(throwable: Throwable) {
        Platform.runLater {
            tableDataGridPane.isVisible = false
            ddlTextArea.showException(throwable)
        }
    }

    private fun fileMenu(mainStage: Stage): Menu {
        val menu = FileMenu(localeService, clientAdapter, uiProperties, storageProperties, mainStage)
        menu.onConnection{ loadTables(it) }
        return menu
    }

    private fun helpMenu(mainStage: Stage): Menu {
        val file = Menu(localeService.getMessage("ui.menu.help"))
        file.isMnemonicParsing = false
        val about = MenuItem(localeService.getMessage("ui.menu.help.about"))
        about.isMnemonicParsing = false
        about.onAction = EventHandler { aboutBox(mainStage) }
        file.items.add(about)
        return file
    }

    fun dataExporter(mainStage: Stage, table: String): DataExportWidget {
        return DataExportWidget(mainStage, table, localeService, uiProperties)
    }

    private fun aboutBox(mainStage: Stage): Stage {
        return AboutBox(mainStage, localeService, uiProperties)
    }
}