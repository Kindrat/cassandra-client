package com.github.kindrat.cassandra.client.ui.window.editor.tables;

import com.github.kindrat.cassandra.client.util.UIUtil;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;

import static com.github.kindrat.cassandra.client.util.UIUtil.buildButton;
import static com.github.kindrat.cassandra.client.util.UIUtil.disable;
import static com.github.kindrat.cassandra.client.util.UIUtil.enable;

class TableButtons extends AnchorPane {

    private final Button plusButton;
    private final Button minusButton;
    private final Button commitButton;
    private final Button cancelButton;

    TableButtons() {
        setMaxHeight(40);
        setMinHeight(40);
        setPrefHeight(40);
        setMaxWidth(250);
        setMinWidth(250);
        setPrefWidth(250);

        GridPane gridPane = gridPane();
        getChildren().add(gridPane);
        UIUtil.fillParent(gridPane);

        plusButton = buildButton("+");
        gridPane.add(plusButton, 0, 0);

        minusButton = buildButton("-");
        gridPane.add(minusButton, 1, 0);

        commitButton = buildButton("✓");
        gridPane.add(commitButton, 3, 0);

        cancelButton = buildButton("∅");
        gridPane.add(cancelButton, 4, 0);
    }

    void enableButtons() {
        enable(plusButton, minusButton, commitButton, cancelButton);
    }

    void disableButtons() {
        disable(plusButton, minusButton, commitButton, cancelButton);
    }

    private GridPane gridPane() {
        GridPane gridPane = new GridPane();
        gridPane.getColumnConstraints().add(new ColumnConstraints(35, 35, 35, Priority.SOMETIMES, HPos.CENTER, true));
        gridPane.getColumnConstraints().add(new ColumnConstraints(35, 35, 35, Priority.SOMETIMES, HPos.CENTER, true));
        gridPane.getColumnConstraints().add(new ColumnConstraints(10, 100, 100, Priority.SOMETIMES, HPos.CENTER, true));
        gridPane.getColumnConstraints().add(new ColumnConstraints(35, 35, 35, Priority.SOMETIMES, HPos.CENTER, true));
        gridPane.getColumnConstraints().add(new ColumnConstraints(35, 35, 35, Priority.SOMETIMES, HPos.CENTER, true));
        gridPane.getColumnConstraints().add(new ColumnConstraints(10, 10, 10, Priority.SOMETIMES, HPos.CENTER, true));

        gridPane.getRowConstraints().add(new RowConstraints(30, 30, 30, Priority.SOMETIMES, VPos.CENTER, true));
        return gridPane;
    }
}
