package com.github.kindrat.cassandra.client.ui.fx

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.TableColumn
import javafx.util.Callback
import java.util.function.Function

object CellValueFactory {
    @JvmStatic
    fun <S, T> create(extractor: Function<S, T>): Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> {
        return Callback { SimpleObjectProperty(extractor.apply(it.value)) }
    }
}