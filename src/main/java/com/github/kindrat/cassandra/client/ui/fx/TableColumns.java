package com.github.kindrat.cassandra.client.ui.fx;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TypeCodec;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import lombok.experimental.UtilityClass;

import static com.github.kindrat.cassandra.client.util.UIUtil.computeTextContainerWidth;

@UtilityClass
public class TableColumns {
    public static <T> TableColumn<T, Object> buildColumn(DataType dataType, String labelText) {
        TableColumn<T, Object> tableColumn = new TableColumn<>();
        Label label = new Label(labelText);
        label.setTooltip(new Tooltip(dataType.asFunctionParameterString()));

        TypeCodec<Object> typeCodec = CodecRegistry.DEFAULT_INSTANCE.codecFor(dataType);
        tableColumn.setCellFactory(CellFactory.create(typeCodec));
        tableColumn.setGraphic(label);
        tableColumn.setMinWidth(computeTextContainerWidth(label.getText(), label.getFont()));
        return tableColumn;
    }

    public static <T, S extends Enum<S>> TableColumn<T, S> buildColumn(Class<S> enumType, String labelText) {
        TableColumn<T, S> tableColumn = new TableColumn<>();
        Label label = new Label(labelText);
        label.setTooltip(new Tooltip(enumType.getSimpleName()));

        tableColumn.setCellFactory(CellFactory.create(enumType));
        tableColumn.setGraphic(label);
        tableColumn.setMinWidth(computeTextContainerWidth(label.getText(), label.getFont()));
        return tableColumn;
    }
}
