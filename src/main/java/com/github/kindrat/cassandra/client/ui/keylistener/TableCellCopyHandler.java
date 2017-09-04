package com.github.kindrat.cassandra.client.ui.keylistener;

import com.github.kindrat.cassandra.client.ui.DataObject;
import javafx.collections.ObservableList;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class TableCellCopyHandler implements Runnable {
    private final TableView<DataObject> tableView;

    @Override
    public void run() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();

        int row = tableView.getSelectionModel().getSelectedIndex();
        ObservableList<TablePosition> selectedCells = tableView.getSelectionModel().getSelectedCells();
        if (!selectedCells.isEmpty()) {
            TablePosition tablePosition = selectedCells.get(0);
            Object cellData = tablePosition.getTableColumn().getCellData(row);
            content.putString(Objects.toString(cellData));
            clipboard.setContent(content);
        }
    }
}
