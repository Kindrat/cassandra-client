package com.github.kindrat.cassandra.client.ui.fx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

@UtilityClass
public class CellValueFactory {
    public static <S, T> Callback<CellDataFeatures<S, T>, ObservableValue<T>> create(Function<S, T> extractor) {
        return param -> {
            S value = param.getValue();
            return new SimpleObjectProperty<>(extractor.apply(value));
        };
    }
}
