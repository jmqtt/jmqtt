package io.jmqtt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by wangkun23 on 2019/12/6.
 */
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {

    @Getter
    @Setter
    private int soBacklog;

    @Getter
    @Setter
    private boolean soReuseaddr;

    @Getter
    @Setter
    private boolean tcpNodelay;

    @Getter
    @Setter
    private boolean soKeepalive;

    @Getter
    @Setter
    private int shannelTimeoutSeconds;

    @Getter
    @Setter
    private boolean epoll;

    @Getter
    @Setter
    private int maxBytesInMessage;



}
