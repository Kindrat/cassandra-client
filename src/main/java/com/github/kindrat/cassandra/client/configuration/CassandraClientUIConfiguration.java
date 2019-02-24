package com.github.kindrat.cassandra.client.configuration;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.properties.StorageProperties;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import com.github.kindrat.cassandra.client.service.CassandraClientAdapter;
import com.github.kindrat.cassandra.client.ui.MainController;
import com.github.kindrat.cassandra.client.ui.View;
import com.github.kindrat.cassandra.client.ui.widget.DataExportWidget;
import com.github.kindrat.cassandra.client.ui.window.editor.main.BackgroundTaskMonitor;
import com.github.kindrat.cassandra.client.ui.window.editor.main.EventLogger;
import com.github.kindrat.cassandra.client.ui.window.editor.main.TableDataGridPane;
import com.github.kindrat.cassandra.client.ui.window.editor.main.filter.FilterGrid;
import com.github.kindrat.cassandra.client.ui.window.editor.main.filter.FilterTextField;
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.DataTableView;
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.PaginationPanel;
import com.github.kindrat.cassandra.client.ui.widget.tableedit.TableEditWidget;
import com.github.kindrat.cassandra.client.ui.window.editor.tables.TablePanel;
import com.github.kindrat.cassandra.client.ui.window.menu.ConnectionDataHandler;
import com.github.kindrat.cassandra.client.ui.window.menu.KeySpaceProvider;
import com.github.kindrat.cassandra.client.ui.window.menu.about.AboutBox;
import com.github.kindrat.cassandra.client.ui.window.menu.file.ConnectionManager;
import com.github.kindrat.cassandra.client.ui.window.menu.file.NewConnectionBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.InputStream;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({CassandraProperties.class, UIProperties.class, StorageProperties.class})
public class CassandraClientUIConfiguration {
    private final MessageByLocaleService localeService;
    private final UIProperties uiProperties;
    private final StorageProperties storageProperties;

    @Bean(name = "mainView")
    public View getMainView() {
        return loadView();
    }

    @Bean
    public MainController getMainController() {
        return (MainController) getMainView().getController();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Stage aboutBox() {
        return new AboutBox(getMainView().getPrimaryStage(), localeService, uiProperties);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Stage tableEditor() {
        return new TableEditWidget(getMainView().getPrimaryStage(), localeService, uiProperties);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Stage dataExporter(String table) {
        return new DataExportWidget(getMainView().getPrimaryStage(), table, localeService, uiProperties);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Stage newConnectionBox(Stage parent, ConnectionDataHandler valueHandler, KeySpaceProvider keyspaceProvider) {
        return new NewConnectionBox(parent, localeService, uiProperties, valueHandler, keyspaceProvider);
    }

    @Bean
    public TableDataGridPane tableDataGridPane() {
        DataTableView dataTableView = new DataTableView();
        FilterGrid filterGrid = new FilterGrid(dataTableView, new FilterTextField(localeService));
        PaginationPanel paginationPanel = new PaginationPanel();
        return new TableDataGridPane(filterGrid, dataTableView, paginationPanel);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public EventLogger eventLogger() {
        return new EventLogger();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Menu fileMenu(CassandraClientAdapter clientAdapter) {
        Menu file = new Menu(localeService.getMessage("ui.menu.file"));
        file.setMnemonicParsing(false);

        MenuItem connect = new MenuItem(localeService.getMessage("ui.menu.file.connect"));
        connect.setMnemonicParsing(false);
        connect.setOnAction(event -> newConnectionBox(getMainView().getPrimaryStage(),
                (data) -> getMainController().loadTables(data), clientAdapter::getAllKeyspaces));

        MenuItem manager = new MenuItem(localeService.getMessage("ui.menu.file.manager"));
        manager.setMnemonicParsing(false);
        manager.setOnAction(event -> connectionManager(clientAdapter::getAllKeyspaces));
        file.getItems().addAll(connect, manager);
        return file;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Menu helpMenu() {
        Menu file = new Menu(localeService.getMessage("ui.menu.help"));
        file.setMnemonicParsing(false);

        MenuItem about = new MenuItem(localeService.getMessage("ui.menu.help.about"));
        about.setMnemonicParsing(false);
        about.setOnAction(event -> aboutBox());

        file.getItems().add(about);
        return file;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ConnectionManager connectionManager(KeySpaceProvider keySpaceProvider) {
        return new ConnectionManager(getMainView().getPrimaryStage(), localeService, keySpaceProvider,
                uiProperties, storageProperties);
    }

    @Bean
    public TablePanel tablePanel() {
        return new TablePanel(uiProperties, localeService, getMainController());
    }

    @Bean
    public BackgroundTaskMonitor backgroundTaskMonitor() {
        return new BackgroundTaskMonitor(uiProperties);
    }

    @SneakyThrows
    private View loadView() {
        try (InputStream fxmlStream = getClass().getClassLoader().getResourceAsStream("app.fxml")) {
            FXMLLoader loader = new FXMLLoader();
            loader.load(fxmlStream);
            return new View(loader.getRoot(), loader.getController());
        }
    }
}
