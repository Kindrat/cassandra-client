package com.github.kindrat.cassandra.client.util;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import static javafx.scene.layout.Region.USE_PREF_SIZE;

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

    public static Button buildButton(String text) {
        Button button = new Button(text);
        button.setMnemonicParsing(false);
        button.setMinWidth(USE_PREF_SIZE);
        button.setMaxWidth(USE_PREF_SIZE);
        button.setPrefWidth(30);

        button.setMinHeight(USE_PREF_SIZE);
        button.setMaxHeight(USE_PREF_SIZE);
        button.setPrefHeight(25);
        return button;
    }

    public static <T extends Region> void setWidth(T node, Number value) {
        setWidth(node, value.doubleValue());
    }

    public static <T extends Region> void setWidth(T node, double value) {
        node.setMaxWidth(value);
        node.setMinWidth(value);
        node.setPrefWidth(value);
    }

    public static double computeTextContainerWidth(String text, Font font) {
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        float width = fontLoader.computeStringWidth(text, font);
        return width * 1.3;
    }
}
