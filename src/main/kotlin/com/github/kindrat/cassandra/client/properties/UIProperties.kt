package com.github.kindrat.cassandra.client.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "client.ui")
class UIProperties(
        val aboutBoxHeight: Double = 70.0,
        val aboutBoxWidth: Double = 300.0,
        val aboutBoxSpacing: Double = 20.0,
        val newConnectHeight: Double = 225.0,
        val newConnectWidth: Double = 250.0,
        val newConnectSpacing: Double = 20.0,
        val credentialsBoxHeight: Double = 93.0,
        val credentialsBoxSpacing: Double = 20.0,
        val connectionManagerHeight: Double = 400.0,
        val connectionManagerWidth: Double = 600.0,
        val connectionManagerRightPaneWidth: Double = 60.0,
        val connectionManagerButtonWidth: Double = 30.0,
        val connectionManagerButtonsSpacing: Double = 50.0,
        val tablesPrefHeight: Double = 100.0,
        val tablesPrefWidth: Double = 250.0,
        val tablesDividerPosition: Double = 0.913,
        val tableEditorHeight: Double = 480.0,
        val tableEditorWidth: Double = 580.0,
        val tableEditorSpacing: Double = 20.0,
        val exportHeight: Double = 480.0,
        val exportWidth: Double = 580.0,
        val exportSpacing: Double = 20.0,

        val defaultDatacenter: String = "datacenter1"
)