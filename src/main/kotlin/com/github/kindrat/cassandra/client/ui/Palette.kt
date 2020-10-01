package com.github.kindrat.cassandra.client.ui

import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Paint

object Palette {
    fun lightGrey(): Background {
        return background("lightgrey")
    }

    fun white(): Background {
        return background("white")
    }

    private fun background(color: String): Background {
        return Background(BackgroundFill(Paint.valueOf(color), CornerRadii(2.0), Insets.EMPTY))
    }
}