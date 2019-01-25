package com.github.kindrat.cassandra.client.ui.window.editor.main;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.service.tasks.TaskStatistics;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Map;
import java.util.UUID;

public class BackgroundTaskPopup extends Stage {

    public BackgroundTaskPopup(Stage parent, Map<UUID, TaskStatistics> taskStatistics,
                               MessageByLocaleService localeService) {

        setTitle(localeService.getMessage("ui.editor.export.title"));
        getIcons().add(new Image("cassandra_ico.png"));
        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setScene(buildScene());

        setMinWidth(200);
        setMinHeight(30);
    }

    private Scene buildScene() {
        return null;
    }
}
