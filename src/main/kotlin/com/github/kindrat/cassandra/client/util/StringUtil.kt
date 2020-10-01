package com.github.kindrat.cassandra.client.util

object StringUtil {
    @JvmStatic
    fun lastWord(words: Array<String>): String? {
        return wordAtPosition(words, -1)
    }

    fun wordAtPosition(words: Array<String>, position: Int): String? {
        return if (position >= 0) {
            if (position < words.size) words[position].trim { it <= ' ' } else null
        } else {
            val index = words.size + position
            if (index >= 0) words[index].trim { it <= ' ' } else null
        }
    }

    fun findClosingBracketIndex(inputString: String): Int {
        var index = 0
        var argIndex = 0
        for (c in inputString.toCharArray()) {
            if (c == '(') {
                argIndex++
            }
            if (c == ')') {
                argIndex--
            }
            if (argIndex == 0) {
                break
            }
            index++
        }
        return index
    }
}