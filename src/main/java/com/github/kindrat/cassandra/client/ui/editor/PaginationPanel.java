package com.github.kindrat.cassandra.client.ui.editor;

import com.github.kindrat.cassandra.client.service.TableContext;
import com.github.kindrat.cassandra.client.ui.DataObject;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.util.function.Consumer;

import static com.github.kindrat.cassandra.client.util.UIUtil.*;

public class PaginationPanel extends GridPane {
    private final Button buttonPrevious;
    private final Button buttonNext;

    public PaginationPanel() {
        setDisabled(true);
        setVisible(true);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPrefWidth(100);
        column1.setMinWidth(10);
        column1.setHalignment(HPos.CENTER);
        column1.setHgrow(Priority.SOMETIMES);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPrefWidth(100);
        column2.setMinWidth(10);
        column2.setHalignment(HPos.CENTER);
        column2.setHgrow(Priority.SOMETIMES);

        getColumnConstraints().clear();
        getColumnConstraints().addAll(column1, column2);

        RowConstraints row = new RowConstraints(10, 30, 30);
        row.setVgrow(Priority.SOMETIMES);

        getRowConstraints().clear();
        getRowConstraints().add(row);

        buttonPrevious = buildButton("←");
        add(buttonPrevious, 0, 0);

        buttonNext = buildButton("→");
        add(buttonNext, 1, 0);
    }

    public void applyOnTable(TableContext context, Consumer<ObservableList<DataObject>> pageConsumer) {
        buttonNext.setOnAction(actionEvent -> {
            disable(buttonNext, buttonPrevious);
            context.nextPage()
                    .thenAccept(pageConsumer)
                    .thenRun(() -> enable(buttonNext, buttonPrevious));
        });
        buttonPrevious.setOnAction(actionEvent -> {
            disable(buttonNext, buttonPrevious);
            context.previousPage()
                    .thenAccept(pageConsumer)
                    .thenRun(() -> enable(buttonNext, buttonPrevious));
        });
    }
}
