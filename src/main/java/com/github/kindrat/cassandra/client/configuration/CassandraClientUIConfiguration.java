package com.github.kindrat.cassandra.client.configuration;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import com.github.kindrat.cassandra.client.ui.MainController;
import com.github.kindrat.cassandra.client.ui.View;
import com.github.kindrat.cassandra.client.ui.editor.EventLogger;
import com.github.kindrat.cassandra.client.ui.editor.FilterTextField;
import com.github.kindrat.cassandra.client.ui.menu.about.AboutBox;
import com.github.kindrat.cassandra.client.ui.menu.file.ConnectionDataHandler;
import com.github.kindrat.cassandra.client.ui.menu.file.NewConnectionBox;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({CassandraProperties.class, UIProperties.class})
public class CassandraClientUIConfiguration {
    private final MessageByLocaleService localeService;
    private final UIProperties uiProperties;

    @Bean(name = "mainView")
    public View getMainView() {
        return loadView();
    }

    @Bean
    public MainController getMainController() throws IOException {
        return (MainController) getMainView().getController();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Stage aboutBox() {
        return new AboutBox(getMainView().getPrimaryStage(), localeService, uiProperties);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Stage newConnectionBox(ConnectionDataHandler valueHandler) {
        return new NewConnectionBox(getMainView().getPrimaryStage(), localeService, uiProperties, valueHandler);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public FilterTextField filterTextField() {
        return new FilterTextField(localeService);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public EventLogger eventLogger() {
        return new EventLogger();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Menu fileMenu(EventHandler<ActionEvent> connectCallback) {
        Menu file = new Menu(localeService.getMessage("ui.menu.file"));
        file.setMnemonicParsing(false);

        MenuItem connect = new MenuItem(localeService.getMessage("ui.menu.file.connect"));
        connect.setMnemonicParsing(false);
        connect.setOnAction(connectCallback);

        file.getItems().add(connect);
        return file;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Menu helpMenu(EventHandler<ActionEvent> aboutCallback) {
        Menu file = new Menu(localeService.getMessage("ui.menu.help"));
        file.setMnemonicParsing(false);

        MenuItem about = new MenuItem(localeService.getMessage("ui.menu.help.about"));
        about.setMnemonicParsing(false);
        about.setOnAction(aboutCallback);

        file.getItems().add(about);
        return file;
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
