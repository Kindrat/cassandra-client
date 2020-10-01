package com.github.kindrat.cassandra.client.ui.window.editor.main.filter

import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata
import com.datastax.oss.driver.shaded.guava.common.collect.Sets
import com.github.kindrat.cassandra.client.filter.Combiner
import com.github.kindrat.cassandra.client.filter.Operator
import com.github.kindrat.cassandra.client.filter.condition.*
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService
import com.github.kindrat.cassandra.client.util.StringUtil.lastWord
import com.github.kindrat.cassandra.client.util.UIUtil.computeTextContainerWidth
import com.github.kindrat.cassandra.client.util.UIUtil.parseWords
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import mu.KotlinLogging
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.function.Consumer

private val logger = KotlinLogging.logger {}

class FilterTextField(localeService: MessageByLocaleService) : TextField() {
    private val stateConditions: MutableList<StateCondition> = ArrayList()
    private val context = ContextMenu()
    private var columnsByName: Map<String, ColumnMetadata>? = null

    fun setTableMetadata(metadata: TableMetadata) {
        reset()
        columnsByName = metadata.columns.mapKeys { it.key.toString() }
    }

    fun suggestCompletion() {
        val words = parseWords(text)
        logger.debug("Checking words : $words")
        val state = stateConditions.find { it.isCurrentState(words, columnsByName!!.keys) }

        if (state != null) {
            logger.debug("Found state ${state.name()}")
            when (state.name()) {
                StateCondition.State.TABLE -> suggestColumnNames()
                StateCondition.State.PARTIAL_TABLE -> suggestColumnName(lastWord(words) ?: "")
                StateCondition.State.OPERATOR -> suggestOperator()
                StateCondition.State.COMBINER -> suggestCombiner()
            }
        } else {
            logger.debug("Unknown state")
        }
    }

    private fun suggestCombiner() {
        val combiners = Combiner.values().map { it.name }.toList()
        rebuildContextMenu(combiners, false)
    }

    private fun suggestOperator() {
        val operators = Operator.values().map(Operator::value).toList()
        rebuildContextMenu(operators, false)
    }

    private fun rebuildContextMenu(values: Collection<String>, isPartial: Boolean) {
        context.items.clear()
        values.forEach(Consumer { value: String ->
            val item = MenuItem(value)
            context.items.add(item)
            item.onAction = EventHandler {
                val text = text
                var words: Array<String> = text.split(" ").toTypedArray()
                if (isPartial) {
                    words[words.size - 1] = value
                } else {
                    words = ArrayUtils.add(words, value)
                }
                var newText = StringUtils.join(words, " ")
                if (!newText.endsWith(" ")) {
                    newText += " "
                }
                setText(newText)
                positionCaret(newText.length)
            }
        })
        val width = computeTextContainerWidth(text, font)
        context.show(this, Side.BOTTOM, width, 0.0)
    }

    private fun suggestColumnName(prefix: String) {
        val columnNames = columnsByName!!.keys
                .filter { StringUtils.startsWithIgnoreCase(it, prefix) }
                .toList()
        rebuildContextMenu(columnNames, true)
    }

    private fun suggestColumnNames() {
        rebuildContextMenu(Sets.union(columnsByName!!.keys, setOf("#")), false)
    }

    private fun reset() {
        text = ""
        columnsByName = null
        context.hide()
        context.items.clear()
    }

    init {
        promptText = localeService.getMessage("ui.editor.filter.textbox.prompt")
        stateConditions.add(TableCondition())
        stateConditions.add(PartialTableCondition())
        stateConditions.add(OperatorCondition())
        stateConditions.add(CombinerCondition())
        GridPane.setMargin(this, Insets(0.0, 10.0, 0.0, 10.0))
    }
}