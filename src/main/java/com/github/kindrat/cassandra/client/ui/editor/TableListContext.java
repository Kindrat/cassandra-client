package com.github.kindrat.cassandra.client.ui.editor;

import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class TableListContext extends ContextMenu {
    private final MessageByLocaleService localeService;
    private final Runnable ddlAction;
    private final Runnable dataAction;

    public TableListContext(MessageByLocaleService localeService, Runnable ddlAction, Runnable dataAction) {
        this.localeService = localeService;
        this.ddlAction = ddlAction;
        this.dataAction = dataAction;
        ObservableList<MenuItem> items = getItems();
        items.add(ddlItem());
        items.add(dataItem());
    }

    private MenuItem ddlItem() {
        MenuItem ddl = new MenuItem(localeService.getMessage("ui.editor.tables.context.ddl"));
        ddl.setOnAction(event -> {
            ddlAction.run();
            TableListContext.this.hide();
        });
        return ddl;
    }

    private MenuItem dataItem() {
        MenuItem data = new MenuItem(localeService.getMessage("ui.editor.tables.context.data"));
        data.setOnAction(event -> {
            dataAction.run();
            TableListContext.this.hide();
        });
        return data;
    }
}
