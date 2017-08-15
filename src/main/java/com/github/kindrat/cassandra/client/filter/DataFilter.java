package com.github.kindrat.cassandra.client.filter;

import com.datastax.driver.core.Row;
import com.github.kindrat.cassandra.client.filter.fields.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.kindrat.cassandra.client.filter.Combiner.AND;
import static com.github.kindrat.cassandra.client.filter.Combiner.OR;
import static com.github.kindrat.cassandra.client.util.StringUtil.findClosingBracketIndex;
import static com.github.kindrat.cassandra.client.util.StringUtil.wordAtPosition;
import static com.github.kindrat.cassandra.client.util.UIUtil.parseWords;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

@Slf4j
public class DataFilter {
    private static Map<Operator, PredicateProvider> predicateFactories = new EnumMap<>(Operator.class);

    static {
        predicateFactories.put(Operator.EQ, EqPredicate::new);
        predicateFactories.put(Operator.GT, GtPredicate::new);
        predicateFactories.put(Operator.GE, GePredicate::new);
        predicateFactories.put(Operator.LT, LtPredicate::new);
        predicateFactories.put(Operator.LE, LePredicate::new);
        predicateFactories.put(Operator.NE, NePredicate::new);
        predicateFactories.put(Operator.LIKE, LikePredicate::new);
    }

    public static Predicate<Row> parsePredicate(String filter) {
        if (filter.startsWith("(")) {
            int closingBracketIndex = findClosingBracketIndex(filter);
            Predicate<Row> predicate = parsePredicate(filter.substring(1, closingBracketIndex));

            if (closingBracketIndex != filter.length() - 1) {
                String secondFilterPart = filter.substring(closingBracketIndex + 1).trim();
                if (startsWithIgnoreCase(secondFilterPart, AND.name())) {
                    return predicate.and(parsePredicate(secondFilterPart.substring(3).trim()));
                } else if (startsWithIgnoreCase(secondFilterPart, OR.name())) {
                    return predicate.or(parsePredicate(secondFilterPart.substring(2).trim()));
                } else {
                    throw new IllegalArgumentException("Illegal combiner : " + secondFilterPart);
                }
            } else {
                return predicate;
            }
        }
        String[] words = parseWords(filter);

        if (words.length < 3) {
            throw new IllegalArgumentException("Incomplete filter : " + filter);
        }
        String firstWord = wordAtPosition(words, 0)
                .orElseThrow(() -> new IllegalArgumentException("Should not be called with empty string"));
        String secondWord = wordAtPosition(words, 1)
                .orElseThrow(() -> new IllegalArgumentException("Should not be called with empty string"));
        String thirdWord = wordAtPosition(words, 2)
                .orElseThrow(() -> new IllegalArgumentException("Should not be called with empty string"));

        Operator operator = Operator.valueOf(secondWord);
        Predicate<Row> operatorPredicate = predicateFactories.get(operator).build(firstWord, thirdWord);

        Optional<String> fourthWord = wordAtPosition(words, 3);
        if (fourthWord.isPresent()) {
            Combiner combiner = Combiner.valueOf(fourthWord.get());
            String[] remainingWords = ArrayUtils.subarray(words, 4, words.length);
            String remainingFilterString = StringUtils.join(remainingWords, " ");
            switch (combiner) {
                case AND:
                    return operatorPredicate.and(parsePredicate(remainingFilterString));
                case OR:
                    return operatorPredicate.or(parsePredicate(remainingFilterString));
            }
        }

        return operatorPredicate;
    }

    @FunctionalInterface
    private interface PredicateProvider {
        RowPredicate build(String field, String value);
    }
}
