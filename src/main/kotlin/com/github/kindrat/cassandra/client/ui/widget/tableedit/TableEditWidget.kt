package com.github.kindrat.cassandra.client.ui.widget.tableedit

import com.datastax.oss.driver.api.core.type.DataType
import com.datastax.oss.driver.api.core.type.DataTypes
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.properties.UIProperties
import com.github.kindrat.cassandra.client.ui.fx.CellValueFactory.create
import com.github.kindrat.cassandra.client.ui.fx.TableColumns.bindTableColumnWidth
import com.github.kindrat.cassandra.client.ui.fx.TableColumns.buildCheckBoxColumn
import com.github.kindrat.cassandra.client.ui.fx.TableColumns.buildColumn
import com.github.kindrat.cassandra.client.ui.fx.component.ToggleSwitch
import com.github.kindrat.cassandra.client.ui.widget.tableedit.ViewDataConverter.fromText
import com.github.kindrat.cassandra.client.ui.widget.tableedit.ViewDataConverter.toText
import com.github.kindrat.cassandra.client.util.CqlUtil
import com.github.kindrat.cassandra.client.util.UIUtil.computeTextContainerWidth
import com.github.kindrat.cassandra.client.util.UIUtil.fillParent
import com.github.kindrat.cassandra.client.util.UIUtil.setWidth
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.stage.Modality
import javafx.stage.Stage
import java.util.*
import java.util.function.Function
import kotlin.math.max

class TableEditWidget(
        parent: Stage,
        private val localeService: MessageByLocaleService,
        private val properties: UIProperties
) : Stage() {

    private val editorHolder = Pane()
    private val rows = FXCollections.observableArrayList<TableRowEntry>()
    private val textView = TextArea()
    private val widgetContext: TableEditWidgetContext
    private val tableNameArea: TextArea
    private val tableView: TableView<TableRowEntry>

    private fun reset() {
        rows.clear()
        rows.add(buildDefaultRow())
    }

    private fun buildScene(): Scene {
        val container = AnchorPane()
        val editor = VBox()
        container.children.add(editor)
        fillParent(editor)
        val dialogScene = Scene(container, properties.tableEditorWidth, properties.tableEditorHeight)
        val sliderLabel = Label(localeService.getMessage("ui.editor.table_editor.slider.label"))
        val textViewLabel = localeService.getMessage("ui.editor.table_editor.slider.text_view")
        val tableViewLabel = localeService.getMessage("ui.editor.table_editor.slider.table_view")
        val toggleSwitch = ToggleSwitch(textViewLabel, tableViewLabel)
        val sliderBox = HBox(sliderLabel, toggleSwitch)
        sliderBox.spacing = 20.0
        sliderBox.alignment = Pos.CENTER_RIGHT
        val tableName = localeService.getMessage("ui.editor.table_editor.table_name.label")
        val newTableNameLabel = Label(tableName)
        newTableNameLabel.minWidth = Region.USE_PREF_SIZE
        newTableNameLabel.maxWidth = Region.USE_PREF_SIZE
        newTableNameLabel.prefWidth = computeTextContainerWidth(tableName, newTableNameLabel.font)
        val tableNameBox = HBox(newTableNameLabel, tableNameArea)
        tableNameBox.spacing = 20.0
        tableNameBox.padding = Insets(2.0, 2.0, 2.0, 10.0)
        tableNameBox.alignment = Pos.CENTER_LEFT
        toggleSwitch.onEnabled {
            editorHolder.children.clear()
            textView.text = toText(rows, tableNameArea.text)
            tableNameBox.isVisible = false
            editorHolder.children.add(textView)
        }
        toggleSwitch.onDisabled {
            editorHolder.children.clear()
            val ddlData = fromText(textView.text)
            tableNameArea.text = ddlData.key
            rows.clear()
            rows.addAll(ddlData.value)
            tableNameBox.isVisible = true
            editorHolder.children.add(tableView)
        }
        val createButton = Button(localeService.getMessage("ui.editor.table_editor.buttons.create"))
        val resetButton = Button(localeService.getMessage("ui.editor.table_editor.buttons.reset"))
        resetButton.onAction = EventHandler { reset() }
        val buttons = HBox(20.0, createButton, resetButton)
        buttons.alignment = Pos.CENTER
        val headerBox = VBox(sliderBox, tableNameBox)
        val children = editor.children
        children.add(headerBox)
        children.add(editorHolder)
        children.add(buttons)
        editorHolder.children.add(tableView)
        dialogScene.heightProperty().addListener { _: ObservableValue<out Number>, _: Number, newValue: Number ->
            tableView.prefHeight = newValue.toDouble() - headerBox.height - buttons.height
            textView.prefHeight = newValue.toDouble() - headerBox.height - buttons.height
        }
        dialogScene.widthProperty().addListener { _: ObservableValue<out Number>, _: Number, newValue: Number ->
            setWidth(editorHolder, newValue)
            setWidth(tableView, newValue)
            setWidth(textView, newValue)
            setWidth(buttons, newValue)
        }
        setWidth(textView, properties.tableEditorWidth)
        onShown = EventHandler {
            val offset = headerBox.height + buttons.height
            textView.prefHeight = properties.tableEditorHeight - offset
        }
        return dialogScene
    }

    private fun buildTableView(): TableView<TableRowEntry> {
        rows.add(buildDefaultRow())
        val view = TableView(rows)
        val nameColumn: TableColumn<TableRowEntry, Any> = buildColumn(DataTypes.TEXT, "Name")
        nameColumn.cellValueFactory = create(Function<TableRowEntry, Any> { it.name })
        nameColumn.setOnEditCommit { it.rowValue.name = it.newValue.toString() }
        bindTableColumnWidth(nameColumn, this, 0.3)
        val typeColumn: TableColumn<TableRowEntry, DataType> = buildColumn(CqlUtil.PRIMITIVE_TYPES, "Type")
        typeColumn.cellValueFactory = create(Function { it.type })
        typeColumn.setOnEditCommit { it.rowValue.type = it.newValue }
        bindTableColumnWidth(typeColumn, this, 0.25)
        val partitionKeyColumn = buildCheckBoxColumn("Partition Key", Function<TableRowEntry, ObservableValue<Boolean>> { it.isPartitionKeyProperty })
        bindTableColumnWidth(partitionKeyColumn, this, 0.1)
        val clusteringKeyColumn = buildCheckBoxColumn("Clustering Key", Function<TableRowEntry, ObservableValue<Boolean>> { it.isClusteringKeyProperty })
        bindTableColumnWidth(clusteringKeyColumn, this, 0.1)
        val indexColumn = buildCheckBoxColumn("Index", Function<TableRowEntry, ObservableValue<Boolean>> { it.hasIndexProperty })
        bindTableColumnWidth(indexColumn, this, 0.1)
        view.columns.add(nameColumn)
        view.columns.add(typeColumn)
        view.columns.add(partitionKeyColumn)
        view.columns.add(clusteringKeyColumn)
        view.columns.add(indexColumn)
        view.isEditable = true
        view.minWidth = properties.tableEditorWidth
        view.maxWidth = properties.tableEditorWidth
        view.columnResizePolicy = TableView.UNCONSTRAINED_RESIZE_POLICY
        view.selectionModel.selectionMode = SelectionMode.SINGLE
        view.onContextMenuRequested = EventHandler {
            val selectionModel = view.selectionModel
            if (!selectionModel.isEmpty || rows.isEmpty()) {
                widgetContext.show(view, it.screenX, it.screenY)
            }
        }
        view.onMouseClicked = EventHandler { event: MouseEvent ->
            if (widgetContext.isShowing) {
                widgetContext.hide()
            }
            val pickResult = event.pickResult
            var source = pickResult.intersectedNode

            // move up through the node hierarchy until a TableRowEntry or scene root is found
            while (source != null && source !is TableRow<*>) {
                source = source.parent
            }

            // clear selection on click anywhere but on a filled row
            if (source == null || source is TableRow<*> && source.isEmpty) {
                view.selectionModel.clearSelection()
            }
        }
        return view
    }

    private fun buildDefaultRow(): TableRowEntry {
        return TableRowEntry(UUID.randomUUID().toString(), DataTypes.TEXT, isPartitionKey = false, isClusteringKey = false, hasIndex = false)
    }

    private fun buildTableArea(): TextArea {
        val textArea = TextArea("new_table")
        textArea.maxHeight = Region.USE_PREF_SIZE
        textArea.minHeight = Region.USE_PREF_SIZE
        textArea.prefHeight = 25.0
        textArea.isWrapText = true
        textArea.prefRowCount = 1
        return textArea
    }

    private fun toRawString(): String {
        return ""
    }

    private fun moveUp() {
        val selectionModel = tableView.selectionModel
        val selected = selectionModel.selectedIndexProperty().get()
        val movingEntry = rows.removeAt(selected)
        val newIndex = selected - 1
        rows.add(newIndex, movingEntry)
        selectionModel.select(newIndex)
    }

    private fun moveDown() {
        val selectionModel = tableView.selectionModel
        val selected = selectionModel.selectedIndexProperty().get()
        val movingEntry = rows.removeAt(selected)
        val newIndex = selected + 1
        if (newIndex == rows.size) {
            rows.add(movingEntry)
        } else {
            rows.add(newIndex, movingEntry)
        }
        selectionModel.select(newIndex)
    }

    init {
        tableView = buildTableView()
        tableNameArea = buildTableArea()
        val selectedIndexProperty = tableView.selectionModel.selectedIndexProperty()
        widgetContext = TableEditWidgetContext(Bindings.size(rows), selectedIndexProperty)
        widgetContext.onDelete { rows.removeAt(selectedIndexProperty.value.toInt()) }
        widgetContext.onAddAbove {
            val index = max(0, selectedIndexProperty.value - 1)
            rows.add(index, buildDefaultRow())
        }
        widgetContext.onAddBelow {
            val selected = selectedIndexProperty.value
            if (selected == rows.size) {
                rows.add(buildDefaultRow())
            } else {
                rows.add(selected + 1, buildDefaultRow())
            }
        }
        widgetContext.onMoveUp { moveUp() }
        widgetContext.onMoveDown { moveDown() }
        title = localeService.getMessage("ui.editor.table_editor.title")
        icons.add(Image("cassandra_ico.png"))
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parent)
        scene = buildScene()
        reset()
        show()
    }
}