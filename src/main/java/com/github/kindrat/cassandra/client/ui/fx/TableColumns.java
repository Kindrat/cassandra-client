package com.github.kindrat.cassandra.client.ui.fx;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TypeCodec;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.stage.Window;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.EnumUtils;

import java.util.List;
import java.util.function.Function;

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

    public static <T> TableColumn<T, Boolean> buildCheckBoxColumn(String labelText,
                                                                  Function<T, ObservableValue<Boolean>> extractor) {
        TableColumn<T, Boolean> tableColumn = new TableColumn<>();
        Label label = new Label(labelText);

        tableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(param -> {
            ObservableList<T> items = tableColumn.getTableView().getItems();
            return extractor.apply(items.get(param));
        }));
        tableColumn.setGraphic(label);
        tableColumn.setMinWidth(computeTextContainerWidth(label.getText(), label.getFont()));
        return tableColumn;
    }

    public static <T, S extends Enum<S>> TableColumn<T, S> buildColumn(Class<S> enumType, String labelText) {
        TableColumn<T, S> tableColumn = new TableColumn<>();
        Label label = new Label(labelText);
        label.setTooltip(new Tooltip(enumType.getSimpleName()));

        List<S> values = EnumUtils.getEnumList(enumType);

        tableColumn.setCellFactory(param -> {
            ComboBoxTableCell<T, S> cell = new ComboBoxTableCell<>(FXCollections.observableArrayList(values));
            cell.setConverter(new EnumStringConverter<>(enumType));
            return cell;
        });
        tableColumn.setGraphic(label);
        tableColumn.setMinWidth(computeTextContainerWidth(label.getText(), label.getFont()));
        return tableColumn;
    }

    public static <T, S> void bindTableColumnWidth(TableColumnBase<S, T> column, Window parent, double percents) {
        ReadOnlyDoubleProperty parentWidth = parent.widthProperty();
        DoubleBinding width = parentWidth.multiply(percents);
        column.setPrefWidth(width.doubleValue());
        column.prefWidthProperty().bind(width);
        column.minWidthProperty().bind(width);
        column.maxWidthProperty().bind(width);
    }

}
