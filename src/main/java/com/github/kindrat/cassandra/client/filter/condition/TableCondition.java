package com.github.kindrat.cassandra.client.filter.condition;

import java.util.Optional;
import java.util.Set;

import static com.github.kindrat.cassandra.client.filter.Combiner.AND;
import static com.github.kindrat.cassandra.client.filter.Combiner.OR;
import static com.github.kindrat.cassandra.client.util.StringUtil.lastWord;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;

public class TableCondition implements StateCondition {
    @Override
    public boolean isCurrentState(String[] words, Set<String> columnNames) {
        Optional<String> lastWord = lastWord(words);
        return !lastWord.isPresent() || equalsAnyIgnoreCase(lastWord.get(), AND.name(), OR.name());
    }

    @Override
    public State name() {
        return State.TABLE;
    }
}
