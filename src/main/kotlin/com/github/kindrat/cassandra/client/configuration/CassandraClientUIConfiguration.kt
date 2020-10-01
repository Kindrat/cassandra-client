package com.github.kindrat.cassandra.client.configuration

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.properties.StorageProperties
import com.github.kindrat.cassandra.client.properties.UIProperties
import com.github.kindrat.cassandra.client.ui.MainController
import com.github.kindrat.cassandra.client.ui.window.editor.main.BackgroundTaskMonitor
import com.github.kindrat.cassandra.client.ui.window.editor.main.EventLogger
import com.github.kindrat.cassandra.client.ui.window.editor.main.TableDataGridPane
import com.github.kindrat.cassandra.client.ui.window.editor.main.filter.FilterGrid
import com.github.kindrat.cassandra.client.ui.window.editor.main.filter.FilterTextField
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.DataTableView
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.PaginationPanel
import com.github.kindrat.cassandra.client.ui.window.editor.tables.TablePanel
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.stage.Stage
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
@ConstructorBinding
@EnableConfigurationProperties(CassandraProperties::class, UIProperties::class, StorageProperties::class)
class CassandraClientUIConfiguration(
        private val localeService: MessageByLocaleService, private val uiProperties: UIProperties
) {

    @Bean
    fun fxmlLoader(): FXMLLoader {
        javaClass.classLoader.getResourceAsStream("app.fxml").use { fxmlStream ->
            val loader = FXMLLoader()
            loader.load<Any>(fxmlStream)
            return loader
        }
    }

    @Bean
    fun mainView(): Parent {
        return fxmlLoader().getRoot()
    }

    @Bean
    fun mainController(): MainController {
        return fxmlLoader().getController()
    }

    @Bean
    fun tableDataGridPane(): TableDataGridPane {
        val dataTableView = DataTableView()
        val filterGrid = FilterGrid(dataTableView, FilterTextField(localeService))
        val paginationPanel = PaginationPanel()
        return TableDataGridPane(filterGrid, dataTableView, paginationPanel)
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun eventLogger(): EventLogger {
        return EventLogger()
    }

    @Bean
    fun tablePanel(): TablePanel {
        return TablePanel(uiProperties, localeService, mainController())
    }

    @Bean
    fun backgroundTaskMonitor(): BackgroundTaskMonitor {
        return BackgroundTaskMonitor(uiProperties)
    }

}