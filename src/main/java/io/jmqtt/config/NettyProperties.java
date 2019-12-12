package io.jmqtt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wangkun23 on 2019/12/6.
 */
@Configuration
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {

    @Getter
    @Setter
    private int soBacklog = 128;

    @Getter
    @Setter
    private Boolean soReuseaddr = true;

    @Getter
    @Setter
    private Boolean tcpNodelay = true;

    @Getter
    @Setter
    private Boolean soKeepalive = true;

    @Getter
    @Setter
    private int channelTimeoutSeconds = 10;

    @Getter
    @Setter
    private Boolean epoll = false;

    @Getter
    @Setter
    private int maxBytesInMessage = 8092;

}
