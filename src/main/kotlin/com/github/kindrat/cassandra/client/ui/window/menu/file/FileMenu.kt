package com.github.kindrat.cassandra.client.ui.window.menu.file

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.properties.StorageProperties
import com.github.kindrat.cassandra.client.properties.UIProperties
import com.github.kindrat.cassandra.client.service.CassandraClientAdapter
import com.github.kindrat.cassandra.client.ui.window.menu.ConnectionDataHandler
import com.github.kindrat.cassandra.client.ui.window.menu.KeySpaceProvider
import javafx.event.EventHandler
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.stage.Stage

class FileMenu(
        private val localeService: MessageByLocaleService,
        private val client: CassandraClientAdapter,
        private val uiProperties: UIProperties,
        private val storageProperties: StorageProperties,
        private val stage: Stage
) : Menu(localeService.getMessage("ui.menu.file")) {
    private val connectItem = MenuItem(localeService.getMessage("ui.menu.file.connect"))
    private val connectManagerItem = MenuItem(localeService.getMessage("ui.menu.file.manager"))

    companion object {
        const val ID = "FileMenu"
    }

    init {
        id = ID
        isMnemonicParsing = false
        connectItem.isMnemonicParsing = false
        connectManagerItem.isMnemonicParsing = false
        connectManagerItem.onAction = EventHandler {
            connectionManager(stage) { url: String, dc: String, username: String?, password: String? ->
                client.getAllKeyspaces(url, dc, username, password)
            }
        }
        items.addAll(connectItem, connectManagerItem)
    }

    fun onConnection(handler: ConnectionDataHandler) {
        connectItem.onAction = EventHandler {
            newConnectionBox(stage, { handler.onConnectionData(it) },
                    { url: String, dc: String, username: String?, password: String? ->
                        client.getAllKeyspaces(url, dc, username, password)
                    })
        }
    }

    private fun newConnectionBox(mainStage: Stage, valueHandler: ConnectionDataHandler,
                                 keyspaceProvider: KeySpaceProvider): Stage {
        return NewConnectionBox(mainStage, localeService, uiProperties, valueHandler, keyspaceProvider)
    }

    private fun connectionManager(mainStage: Stage, keySpaceProvider: KeySpaceProvider): ConnectionManager {
        return ConnectionManager(mainStage, localeService, keySpaceProvider, uiProperties, storageProperties)
    }
}