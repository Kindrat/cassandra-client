package com.github.kindrat.cassandra.client.util;

import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class StringUtil {
    public static Optional<String> lastWord(String[] words) {
        return wordAtPosition(words, -1);
    }

    public static Optional<String> wordAtPosition(String[] words, int position) {
        if (position >= 0) {
            return position < words.length ? Optional.of(words[position].trim()) : Optional.empty();
        } else {
            int index = words.length + position;
            return index >= 0 ? Optional.of(words[index].trim()) : Optional.empty();
        }
    }

    public static int findClosingBracketIndex(String inputString) {
        int index = 0;
        int argIndex = 0;

        for (char c : inputString.toCharArray()) {
            if (c == '(') {
                argIndex++;
            }
            if (c == ')') {
                argIndex--;
            }
            if (argIndex == 0) {
                break;
            }
            index++;
        }
        return index;
    }
}
