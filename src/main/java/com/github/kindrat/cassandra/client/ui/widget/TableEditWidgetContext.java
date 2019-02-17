package com.github.kindrat.cassandra.client.ui.widget;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PopupControl;

class TableEditWidgetContext extends ContextMenu {
    private final Property<Integer> tableSizeProperty = new SimpleObjectProperty<>(0);
    private final Property<Integer> selectedIndexProperty = new SimpleObjectProperty<>(-1);

    private final MenuItem insertAbove;
    private final MenuItem insertBelow;
    private final MenuItem delete;
    private final MenuItem moveUp;
    private final MenuItem moveDown;

    TableEditWidgetContext(IntegerBinding tableSizeBinding, ReadOnlyIntegerProperty selectedIndexProperty) {
        insertAbove = new MenuItem("Insert row above");
        insertBelow = new MenuItem("Insert row below");
        delete = new MenuItem("Delete row");
        moveUp = new MenuItem("Move up");
        moveDown = new MenuItem("Move down");

        setMaxWidth(PopupControl.USE_PREF_SIZE);
        setMinWidth(PopupControl.USE_PREF_SIZE);
        setMaxHeight(PopupControl.USE_PREF_SIZE);
        setMinHeight(PopupControl.USE_PREF_SIZE);
        setPrefSize(300, 200);

        tableSizeBinding.addListener((observable, oldValue, newValue) ->
                this.tableSizeProperty.setValue(newValue.intValue()));
        selectedIndexProperty.addListener((observable, oldValue, newValue) ->
                this.selectedIndexProperty.setValue(newValue.intValue()));

        resetMenuItems();
        initItemMask();
    }

    void onAddAbove(Runnable action) {
        insertAbove.setOnAction(event -> {
            action.run();
            hide();
        });
    }

    void onAddBelow(Runnable action) {
        insertBelow.setOnAction(event -> {
            action.run();
            hide();
        });
    }

    void onDelete(Runnable action) {
        delete.setOnAction(event -> {
            action.run();
            hide();
        });
    }

    void onMoveUp(Runnable action) {
        moveUp.setOnAction(event -> {
            action.run();
            hide();
        });
    }

    void onMoveDown(Runnable action) {
        moveDown.setOnAction(event -> {
            action.run();
            hide();
        });
    }

    private void initItemMask() {
        selectedIndexProperty.addListener((observable, oldValue, newValue) -> {
            resetMenuItems();
            ObservableList<MenuItem> menuItems = getItems();
            if (newValue == 0) {
                menuItems.remove(moveUp);
            }
            if (newValue.equals(tableSizeProperty.getValue() - 1)) {
                menuItems.remove(moveDown);
            }
            if (tableSizeProperty.getValue() == 0) {
                menuItems.remove(delete);
            }
        });
    }

    private void resetMenuItems() {
        getItems().clear();
        getItems().addAll(insertAbove, insertBelow, delete, moveUp, moveDown);
    }
}
