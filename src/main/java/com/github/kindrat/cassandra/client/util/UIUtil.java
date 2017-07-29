package com.github.kindrat.cassandra.client.util;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UIUtil {
    public static void fillParent(Node node) {
        AnchorPane.setLeftAnchor(node, 0.);
        AnchorPane.setRightAnchor(node, 0.);
        AnchorPane.setTopAnchor(node, 0.);
        AnchorPane.setBottomAnchor(node, 0.);
    }
}
