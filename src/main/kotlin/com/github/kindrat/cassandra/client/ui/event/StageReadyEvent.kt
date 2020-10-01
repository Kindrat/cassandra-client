package com.github.kindrat.cassandra.client.ui.event

import javafx.stage.Stage
import org.springframework.context.ApplicationEvent

class StageReadyEvent(val stage: Stage): ApplicationEvent(stage)