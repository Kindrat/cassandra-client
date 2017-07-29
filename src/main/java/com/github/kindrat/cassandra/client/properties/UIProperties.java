package com.github.kindrat.cassandra.client.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "client.ui")
public class UIProperties {
    private Integer aboutBoxHeight = 70;
    private Integer aboutBoxWidth = 300;
    private Integer aboutBoxSpacing = 20;

    private Integer newConnectHeight = 150;
    private Integer newConnectWidth = 250;
    private Integer newConnectSpacing = 20;
}
