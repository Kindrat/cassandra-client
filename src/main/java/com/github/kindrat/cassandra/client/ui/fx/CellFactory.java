package com.github.kindrat.cassandra.client.ui.fx;

import com.datastax.driver.core.TypeCodec;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CellFactory {
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> create(TypeCodec<T> codec) {
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
