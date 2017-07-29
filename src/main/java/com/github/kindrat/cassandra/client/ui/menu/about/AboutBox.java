package com.github.kindrat.cassandra.client.ui.menu.about;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.properties.UIProperties;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AboutBox extends Stage {
    private final MessageByLocaleService localeService;
    private final UIProperties properties;

    public AboutBox(Stage parent, MessageByLocaleService localeService, UIProperties properties) {
        this.localeService = localeService;
        this.properties = properties;
        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setScene(buildScene());
        show();
    }

    private Scene buildScene() {
        VBox aboutBox = new VBox(properties.getAboutBoxSpacing());
        Scene dialogScene = new Scene(aboutBox, properties.getAboutBoxWidth(), properties.getAboutBoxHeight());
        Text text = new Text(localeService.getMessage("ui.menu.help.about.text"));
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(properties.getAboutBoxWidth());
        aboutBox.getChildren().add(text);
        return dialogScene;
    }
}
