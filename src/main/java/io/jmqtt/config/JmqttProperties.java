package io.jmqtt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wangkun23 on 2019/12/6.
 */
@Configuration
@ConfigurationProperties(prefix = "jmqtt")
public class JmqttProperties {

    @Getter
    @Setter
    private int port = 1883;

    @Getter
    @Setter
    private String host = "0.0.0.0";
}
