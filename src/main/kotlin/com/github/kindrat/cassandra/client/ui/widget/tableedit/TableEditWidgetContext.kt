package com.github.kindrat.cassandra.client.ui.widget.tableedit

import javafx.beans.binding.IntegerBinding
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.PopupControl

internal class TableEditWidgetContext(
        tableSizeBinding: IntegerBinding,
        selectedIndexProperty: ReadOnlyIntegerProperty
) : ContextMenu() {

    private val tableSizeProperty: Property<Int> = SimpleObjectProperty(0)
    private val selectedIndexProperty: Property<Int> = SimpleObjectProperty(-1)
    private val insertAbove: MenuItem = MenuItem("Insert row above")
    private val insertBelow: MenuItem = MenuItem("Insert row below")
    private val delete: MenuItem = MenuItem("Delete row")
    private val moveUp: MenuItem = MenuItem("Move up")
    private val moveDown: MenuItem = MenuItem("Move down")

    fun onAddAbove(action: Runnable) {
        insertAbove.onAction = EventHandler {
            action.run()
            hide()
        }
    }

    fun onAddBelow(action: Runnable) {
        insertBelow.onAction = EventHandler {
            action.run()
            hide()
        }
    }

    fun onDelete(action: Runnable) {
        delete.onAction = EventHandler {
            action.run()
            hide()
        }
    }

    fun onMoveUp(action: Runnable) {
        moveUp.onAction = EventHandler {
            action.run()
            hide()
        }
    }

    fun onMoveDown(action: Runnable) {
        moveDown.onAction = EventHandler {
            action.run()
            hide()
        }
    }

    private fun initItemMask() {
        selectedIndexProperty.addListener { _: ObservableValue<out Int>, _: Int, newValue: Int ->
            resetMenuItems()
            val menuItems = items
            if (newValue == 0) {
                menuItems.remove(moveUp)
            }
            if (newValue == tableSizeProperty.value - 1) {
                menuItems.remove(moveDown)
            }
            if (tableSizeProperty.value == 0) {
                menuItems.remove(delete)
            }
        }
    }

    private fun resetMenuItems() {
        items.clear()
        items.addAll(insertAbove, insertBelow, delete, moveUp, moveDown)
    }

    init {
        maxWidth = PopupControl.USE_PREF_SIZE
        minWidth = PopupControl.USE_PREF_SIZE
        maxHeight = PopupControl.USE_PREF_SIZE
        minHeight = PopupControl.USE_PREF_SIZE
        setPrefSize(300.0, 200.0)
        tableSizeBinding.addListener { _: ObservableValue<out Number>, _: Number, newValue: Number -> tableSizeProperty.setValue(newValue.toInt()) }
        selectedIndexProperty.addListener { _: ObservableValue<out Number>, _: Number, newValue: Number -> this.selectedIndexProperty.setValue(newValue.toInt()) }
        resetMenuItems()
        initItemMask()
    }
}