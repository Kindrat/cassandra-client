package com.github.kindrat.cassandra.client.ui.fx.component;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LabeledComponentColumn extends VBox {
    private final double spacing;

    public LabeledComponentColumn(double spacing) {
        this.spacing = spacing;
        setSpacing(spacing);
    }

    public void addLabeledElement(String label, Node element) {
        HBox row = new HBox(spacing);

        VBox labelColumn = new VBox(spacing);
        labelColumn.setAlignment(Pos.CENTER_LEFT);
        labelColumn.prefWidthProperty().bind(widthProperty().multiply(0.6));
//        UIUtil.setWidth(labelColumn, width * 0.6);
        labelColumn.getChildren().add(new Label(label));

        VBox elementColumn = new VBox(spacing);
        elementColumn.setAlignment(Pos.CENTER_LEFT);
        elementColumn.prefWidthProperty().bind(widthProperty().multiply(0.25));
//        UIUtil.setWidth(elementColumn, width * 0.25);
        elementColumn.getChildren().add(element);

        row.getChildren().addAll(labelColumn, elementColumn);
        getChildren().add(row);
    }
}
