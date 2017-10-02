package com.github.kindrat.cassandra.client.util;

import com.datastax.driver.core.TypeCodec;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
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

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> cellFactory(
            TypeCodec<T> codec) {
        return TextFieldTableCell.forTableColumn(new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return codec.format(object);
            }

            @Override
            public T fromString(String string) {
                return codec.parse(string);
            }
        });
    }
}
