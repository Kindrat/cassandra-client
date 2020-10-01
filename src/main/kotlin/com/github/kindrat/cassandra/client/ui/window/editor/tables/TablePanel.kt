package com.github.kindrat.cassandra.client.ui.window.editor.tables

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.properties.UIProperties
import com.github.kindrat.cassandra.client.ui.MainController
import com.github.kindrat.cassandra.client.util.UIUtil.fillParent
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.AnchorPane
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import java.util.function.Consumer

class TablePanel(
        uiProperties: UIProperties,
        localeService: MessageByLocaleService,
        private val controller: MainController
) : AnchorPane(), BeanFactoryAware {

    companion object {
        const val ID = "RootTablePanel"
    }

    private val tableListView: TableListView
    private val buttons: TableButtons
    private val tableContext: TableListContext

    fun showTables(tables: ObservableList<String>) {
        tableListView.showTables(tables)
    }

    fun clear() {
        tableContext.hide()
        tableListView.clear()
    }

    fun setNewValueListener(newSelectedTableListener: Consumer<String?>) {
        tableListView.onNewValueSelected { _: ObservableValue<out String?>?, _: String?, newValue: String? ->
            if (newValue != null) {
                newSelectedTableListener.accept(newValue)
            }
            tableContext.hide()
        }
    }

    @Throws(BeansException::class)
    override fun setBeanFactory(beanFactory: BeanFactory) {
        buttons.initActions(beanFactory)
    }

    private fun splitPane(uiProperties: UIProperties): SplitPane {
        val splitPane = SplitPane(tableListView, buttons)
        splitPane.setDividerPositions(uiProperties.tablesDividerPosition)
        splitPane.isFocusTraversable = false
        splitPane.maxHeight = 40.0
        splitPane.prefHeight = 40.0
        splitPane.prefWidth = 160.0
        splitPane.isScaleShape = false
        splitPane.orientation = Orientation.VERTICAL
        return splitPane
    }

    private fun tryLoadDDL() {
        tableListView.selectedTable?.apply { controller.showDDLForTable(this) }
    }

    private fun tryLoadData() {
        tableListView.selectedTable?.apply { controller.showDataForTable(this) }
    }

    private fun tryExportData() {
        tableListView.selectedTable?.apply { controller.exportDataForTable(this) }
    }

    private fun onTableContextMenu(event: ContextMenuEvent) {
        tableListView.selectedTable?.apply { tableContext.show(this@TablePanel, event.screenX, event.screenY) }
    }

    init {
        id = ID
        prefHeight = uiProperties.tablesPrefHeight
        prefWidth = uiProperties.tablesPrefWidth
        buttons = TableButtons()
        tableListView = TableListView()
        tableListView.onMouseClick({ buttons.enableButtons() }, { buttons.disableButtons() }, { tryLoadData() })
        tableListView.onContextMenuRequested = EventHandler { onTableContextMenu(it) }
        val splitPane = splitPane(uiProperties)
        fillParent(splitPane)
        children.add(splitPane)
        tableContext = TableListContext(localeService)
        tableContext.onDataAction { tryLoadData() }
        tableContext.onDdlAction { tryLoadDDL() }
        tableContext.onExportAction { tryExportData() }
    }
}