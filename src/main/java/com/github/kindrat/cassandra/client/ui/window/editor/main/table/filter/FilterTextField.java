package com.github.kindrat.cassandra.client.ui.window.editor.main.table.filter;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.TableMetadata;
import com.github.kindrat.cassandra.client.filter.Combiner;
import com.github.kindrat.cassandra.client.filter.Operator;
import com.github.kindrat.cassandra.client.filter.condition.*;
import com.github.kindrat.cassandra.client.i18n.MessageByLocaleService;
import com.github.kindrat.cassandra.client.util.UIUtil;
import com.google.common.collect.Sets;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.kindrat.cassandra.client.util.StreamUtils.toMap;
import static com.github.kindrat.cassandra.client.util.StringUtil.lastWord;
import static java.util.Arrays.stream;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

@Slf4j
public class FilterTextField extends TextField {
    private final List<StateCondition> stateConditions = new ArrayList<>();
    private final ContextMenu context = new ContextMenu();
    private Map<String, ColumnMetadata> columnsByName;

    public FilterTextField(MessageByLocaleService localeService) {
        setPromptText(localeService.getMessage("ui.editor.filter.textbox.prompt"));
        stateConditions.add(new TableCondition());
        stateConditions.add(new PartialTableCondition());
        stateConditions.add(new OperatorCondition());
        stateConditions.add(new CombinerCondition());
    }

    public void setTableMetadata(TableMetadata metadata) {
        reset();
        columnsByName = toMap(metadata.getColumns(), ColumnMetadata::getName);
    }

    public void suggestCompletion() {
        String[] words = UIUtil.parseWords(getText());
        log.info("Checking words : {}", Arrays.toString(words));
        Optional<StateCondition> state = stateConditions.stream()
                .filter(stateCondition -> stateCondition.isCurrentState(words, columnsByName.keySet()))
                .findFirst();

        if (state.isPresent()) {
            StateCondition stateCondition = state.get();
            log.info("Found state {}", stateCondition.name());
            switch (stateCondition.name()) {
                case TABLE:
                    suggestColumnNames();
                    break;
                case PARTIAL_TABLE:
                    suggestColumnName(lastWord(words).orElse(""));
                    break;
                case OPERATOR:
                    suggestOperator();
                    break;
                case COMBINER:
                    suggestCombiner();
                    break;
            }
        } else {
            log.info("Unknown state");
        }
    }

    private void suggestCombiner() {
        List<String> combiners = stream(Combiner.values()).map(Enum::name).collect(Collectors.toList());
        rebuildContextMenu(combiners, false);
    }

    private void suggestOperator() {
        List<String> operators = stream(Operator.values()).map(Operator::getValue).collect(Collectors.toList());
        rebuildContextMenu(operators, false);
    }

    private void rebuildContextMenu(Collection<String> values, boolean isPartial) {
        context.getItems().clear();
        values.forEach(value -> {
            MenuItem item = new MenuItem(value);
            context.getItems().add(item);
            item.setOnAction(event -> {
                String text = getText();
                String[] words = text.split(" ");

                if (isPartial) {
                    words[words.length - 1] = value;
                } else {
                    words = ArrayUtils.add(words, value);
                }
                String newText = StringUtils.join(words, " ");
                if (!newText.endsWith(" ")) {
                    newText += " ";
                }
                setText(newText);
                positionCaret(newText.length());
            });
        });

        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        float width = fontLoader.computeStringWidth(getText(), getFont());
        context.show(this, Side.BOTTOM, width, 0);
    }

    private void suggestColumnName(String prefix) {
        List<String> columnNames = columnsByName.keySet().stream()
                .filter(name -> startsWithIgnoreCase(name, prefix))
                .collect(Collectors.toList());
        rebuildContextMenu(columnNames, true);
    }

    private void suggestColumnNames() {
        rebuildContextMenu(Sets.union(columnsByName.keySet(), singleton("#")), false);
    }

    private void reset() {
        setText("");
        columnsByName = null;
        context.hide();
        context.getItems().clear();
    }
}
