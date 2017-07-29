package com.github.kindrat.cassandra.client.ui.editor;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;


public class TableActionsPanel extends GridPane {

    public TableActionsPanel() {
        addColumn(0);
        addColumn(1);
        addColumn(2);
        addColumn(3);

        addRow(0);
        addRow(1);
        addRow(2);

        add(loadDDLButton(), 1, 1);
        add(showDataButton(), 2, 1);
    }

    private Button loadDDLButton() {
        Button button = new Button("DDL");
        return button;
    }

    private Button showDataButton() {
        Button button = new Button("Data");
        return button;
    }
}
