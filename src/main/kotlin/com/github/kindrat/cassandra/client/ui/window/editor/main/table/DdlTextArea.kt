package com.github.kindrat.cassandra.client.ui.window.editor.main.table

import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata
import com.github.kindrat.cassandra.client.util.UIUtil.fillParent
import javafx.scene.control.TextArea
import org.apache.commons.lang3.exception.ExceptionUtils

class DdlTextArea : TextArea() {
    fun showTableDDL(tableMetadata: TableMetadata) {
//        setText(formatDDL(tableMetadata.describeWithChildren(true)));
        text = tableMetadata.describeWithChildren(true)
        isVisible = true
    }

    fun showException(throwable: Throwable?) {
        text = ExceptionUtils.getStackTrace(throwable)
        isVisible = true
    }

    init {
        isEditable = false
        setPrefSize(200.0, 200.0)
        isVisible = false
        fillParent(this)
    }
}