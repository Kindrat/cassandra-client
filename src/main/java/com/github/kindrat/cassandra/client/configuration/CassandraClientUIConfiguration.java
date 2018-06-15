package com.github.kindrat.cassandra.client.configuration;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.properties.StorageProperties;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import com.github.kindrat.cassandra.client.ui.MainController;
import com.github.kindrat.cassandra.client.ui.View;
import com.github.kindrat.cassandra.client.ui.window.editor.main.EventLogger;
import com.github.kindrat.cassandra.client.ui.window.editor.main.filter.FilterGrid;
import com.github.kindrat.cassandra.client.ui.window.editor.main.filter.FilterTextField;
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.DataTableView;
import com.github.kindrat.cassandra.client.ui.window.editor.main.table.PaginationPanel;
import com.github.kindrat.cassandra.client.ui.window.editor.tables.TablePanel;
import com.github.kindrat.cassandra.client.ui.window.menu.ConnectionDataHandler;
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
    public Stage newConnectionBox(Stage parent, ConnectionDataHandler valueHandler) {
        return new NewConnectionBox(parent, localeService, uiProperties, valueHandler);
    }

    @Bean
    public DataTableView dataTableView() {
        return new DataTableView();
    }

    @Bean
    public FilterGrid filterGrid() {
        return new FilterGrid(dataTableView(), new FilterTextField(localeService));
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public EventLogger eventLogger() {
        return new EventLogger();
    }

    @Bean
    public PaginationPanel paginationPanel() {
        return new PaginationPanel();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Menu fileMenu() {
        Menu file = new Menu(localeService.getMessage("ui.menu.file"));
        file.setMnemonicParsing(false);

        MenuItem connect = new MenuItem(localeService.getMessage("ui.menu.file.connect"));
        connect.setMnemonicParsing(false);
        connect.setOnAction(event -> newConnectionBox(getMainView().getPrimaryStage(),
                (data) -> getMainController().loadTables(data)));

        MenuItem manager = new MenuItem(localeService.getMessage("ui.menu.file.manager"));
        manager.setMnemonicParsing(false);
        manager.setOnAction(event -> connectionManager());
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
    public ConnectionManager connectionManager() {
        return new ConnectionManager(getMainView().getPrimaryStage(), localeService, uiProperties, storageProperties);
    }

    @Bean
    public TablePanel tablePanel() {
        return new TablePanel(uiProperties, localeService, getMainController());
    }

    @SneakyThrows
    private View loadView() {
        InputStream fxmlStream = null;
        try {
            fxmlStream = getClass().getClassLoader().getResourceAsStream("app.fxml");
            FXMLLoader loader = new FXMLLoader();
            loader.load(fxmlStream);
            return new View(loader.getRoot(), loader.getController());
        } finally {
            if (fxmlStream != null) {
                fxmlStream.close();
            }
        }
    }
}
