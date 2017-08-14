package com.github.kindrat.cassandra.client.ui.editor.filter;

import java.util.Optional;
import java.util.Set;

public interface StateCondition {
    boolean isCurrentState(String[] words, Set<String> columnNames);

    State name();

    default Optional<String> lastWord(String[] words) {
        return wordAtPosition(words, -1);
    }

    default Optional<String> wordAtPosition(String[] words, int position) {
        if (position >= 0) {
            return position < words.length ? Optional.of(words[position].trim()) : Optional.empty();
        } else {
            int index = words.length + position;
            return index >= 0 ? Optional.of(words[index].trim()) : Optional.empty();
        }
    }

    enum State {
        TABLE, PARTIAL_TABLE, OPERATOR, COMBINER
    }
}
