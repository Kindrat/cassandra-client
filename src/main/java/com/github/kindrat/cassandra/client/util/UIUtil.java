package com.github.kindrat.cassandra.client.util;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class UIUtil {
    public static void fillParent(Node node) {
        AnchorPane.setLeftAnchor(node, 0.);
        AnchorPane.setRightAnchor(node, 0.);
        AnchorPane.setTopAnchor(node, 0.);
        AnchorPane.setBottomAnchor(node, 0.);
    }

    public static String[] parseWords(String rawString) {
        return StringUtils.split(StringUtils.defaultString(rawString, ""));
    }

    public static void disable(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(true);
        }
    }

    public static void enable(Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(false);
        }
    }
}
