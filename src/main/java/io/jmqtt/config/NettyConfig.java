package io.jmqtt.config;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wangkun23 on 2019/12/6.
 */
@Configuration
public class NettyConfig {
    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * bossGroup
     *
     * @return
     */
    @Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
    public EventLoopGroup bossGroup() {
        return new NioEventLoopGroup();
    }

    /**
     * workerGroup
     *
     * @return
     */
    @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    public EventLoopGroup workerGroup() {
        return new NioEventLoopGroup();
    }
}
