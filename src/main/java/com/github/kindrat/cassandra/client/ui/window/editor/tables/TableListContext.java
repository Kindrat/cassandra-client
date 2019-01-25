package com.github.kindrat.cassandra.client.ui.window.editor.tables;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

class TableListContext extends ContextMenu {
    private final MenuItem ddlItem;
    private final MenuItem dataItem;
    private final MenuItem exportItem;

    TableListContext(MessageByLocaleService localeService) {
        ddlItem = new MenuItem(localeService.getMessage("ui.editor.tables.context.ddl"));
        dataItem = new MenuItem(localeService.getMessage("ui.editor.tables.context.data"));
        exportItem = new MenuItem(localeService.getMessage("ui.editor.tables.context.export"));

        ObservableList<MenuItem> items = getItems();
        items.add(ddlItem);
        items.add(dataItem);
        items.add(exportItem);
    }

    public void onDdlAction(Runnable action) {
        setAction(ddlItem, action);
    }

    public void onDataAction(Runnable action) {
        setAction(dataItem, action);
    }

    public void onExportAction(Runnable action) {
        setAction(exportItem, action);
    }

    private void setAction(MenuItem item, Runnable action) {
        item.setOnAction(event -> {
            action.run();
            TableListContext.this.hide();
        });
    }
}
