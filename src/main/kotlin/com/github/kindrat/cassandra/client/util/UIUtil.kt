package com.github.kindrat.cassandra.client.util

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import javafx.scene.text.Font
import javafx.scene.text.Text
import org.apache.commons.lang3.StringUtils

object UIUtil {
    @JvmStatic
    fun fillParent(node: Node) {
        AnchorPane.setLeftAnchor(node, 0.0)
        AnchorPane.setRightAnchor(node, 0.0)
        AnchorPane.setTopAnchor(node, 0.0)
        AnchorPane.setBottomAnchor(node, 0.0)
    }

    @JvmStatic
    fun parseWords(rawString: String): Array<String> {
        return StringUtils.split(StringUtils.defaultString(rawString, ""))
    }

    @JvmStatic
    fun disable(vararg nodes: Node) {
        for (node in nodes) {
            node.isDisable = true
        }
    }

    @JvmStatic
    fun enable(vararg nodes: Node) {
        for (node in nodes) {
            node.isDisable = false
        }
    }

    @JvmStatic
    fun buildButton(text: String): Button {
        val button = Button(text)
        button.isMnemonicParsing = false
        button.minWidth = Region.USE_PREF_SIZE
        button.maxWidth = Region.USE_PREF_SIZE
        button.prefWidth = 30.0
        button.minHeight = Region.USE_PREF_SIZE
        button.maxHeight = Region.USE_PREF_SIZE
        button.prefHeight = 25.0
        return button
    }

    fun <T : Region?> setWidth(node: T, value: Number) {
        setWidth(node, value.toDouble())
    }

    @JvmStatic
    fun <T : Region?> setWidth(node: T, value: Double) {
        node!!.maxWidth = value
        node.minWidth = value
        node.prefWidth = value
    }

    @JvmStatic
    fun computeTextContainerWidth(text: String, font: Font): Double {
        val theText = Text(text)
        theText.font = font
        val width = theText.boundsInLocal.width
        return width * 1.3
    }
}