package com.github.kindrat.cassandra.client.ui.widget.tableedit

import com.datastax.oss.driver.api.core.type.DataType
import javafx.beans.property.*

class TableRowEntry(
        name: String,
        type: DataType,
        isPartitionKey: Boolean,
        isClusteringKey: Boolean,
        hasIndex: Boolean
) {
    private val nameProperty: StringProperty = SimpleStringProperty("")
    private val typeProperty: ObjectProperty<DataType> = SimpleObjectProperty()
    val isPartitionKeyProperty: BooleanProperty = SimpleBooleanProperty(false)
    val isClusteringKeyProperty: BooleanProperty = SimpleBooleanProperty(false)
    val hasIndexProperty: BooleanProperty = SimpleBooleanProperty(false)

    var name: String
        get() = nameProperty.get()
        set(name) {
            nameProperty.value = name
        }

    var type: DataType
        get() = typeProperty.get()
        set(type) {
            typeProperty.value = type
        }

    var isPartitionKey: Boolean
        get() = isPartitionKeyProperty.get()
        set(isPartitionKey) {
            isPartitionKeyProperty.value = isPartitionKey
        }

    var isClusteringKey: Boolean
        get() = isClusteringKeyProperty.get()
        set(isClusteringKey) {
            isClusteringKeyProperty.value = isClusteringKey
        }

    fun setHasIndex(hasIndex: Boolean) {
        hasIndexProperty.value = hasIndex
    }

    fun hasIndex(): Boolean {
        return hasIndexProperty.get()
    }

    init {
        nameProperty.value = name
        typeProperty.value = type
        isPartitionKeyProperty.value = isPartitionKey
        isClusteringKeyProperty.value = isClusteringKey
        hasIndexProperty.value = hasIndex
    }
}