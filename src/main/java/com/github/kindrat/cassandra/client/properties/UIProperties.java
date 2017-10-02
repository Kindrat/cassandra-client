package com.github.kindrat.cassandra.client.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "client.ui")
public class UIProperties {
    private Integer aboutBoxHeight = 70;
    private Integer aboutBoxWidth = 300;
    private Integer aboutBoxSpacing = 20;

    private Integer newConnectHeight = 175;
    private Integer newConnectWidth = 250;
    private Integer newConnectSpacing = 20;

    private Integer credentialsBoxHeight = 93;
    private Integer credentialsBoxSpacing = 20;

    private Integer connectionManagerHeight = 400;
    private Integer connectionManagerWidth = 600;
    private Integer connectionManagerRightPaneWidth = 60;
    private Integer connectionManagerButtonWidth = 30;
    private Integer connectionManagerButtonsSpacing = 50;
}
