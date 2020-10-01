package com.github.kindrat.cassandra.client.ui.window.editor.tables

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem

internal class TableListContext(localeService: MessageByLocaleService) : ContextMenu() {
    private val ddlItem: MenuItem = MenuItem(localeService.getMessage("ui.editor.tables.context.ddl"))
    private val dataItem: MenuItem = MenuItem(localeService.getMessage("ui.editor.tables.context.data"))
    private val exportItem: MenuItem = MenuItem(localeService.getMessage("ui.editor.tables.context.export"))

    fun onDdlAction(action: Runnable) {
        setAction(ddlItem, action)
    }

    fun onDataAction(action: Runnable) {
        setAction(dataItem, action)
    }

    fun onExportAction(action: Runnable) {
        setAction(exportItem, action)
    }

    private fun setAction(item: MenuItem, action: Runnable) {
        item.onAction = EventHandler {
            action.run()
            hide()
        }
    }

    init {
        val items = items
        items.add(ddlItem)
        items.add(dataItem)
        items.add(exportItem)
    }
}