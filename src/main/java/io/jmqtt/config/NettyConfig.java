package io.jmqtt.config;

import io.jmqtt.BrokerConstants;
import io.jmqtt.broker.*;
import io.jmqtt.broker.config.IConfig;
import io.jmqtt.broker.security.IAuthenticator;
import io.jmqtt.broker.security.IAuthorizatorPolicy;
import io.jmqtt.broker.subscriptions.CTrieSubscriptionDirectory;
import io.jmqtt.broker.subscriptions.ISubscriptionsDirectory;
import io.jmqtt.interception.InterceptHandler;
import io.jmqtt.persistence.H2Builder;
import io.jmqtt.persistence.MemorySubscriptionsRepository;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static io.jmqtt.logging.LoggingUtils.getInterceptorIds;

/**
 * Created by wangkun23 on 2019/12/6.
 */
//@Configuration
public class NettyConfig {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private ScheduledExecutorService scheduler;

    @Resource
    private NettyProperties nettyProperties;

    private PostOffice dispatcher;
    private H2Builder h2Builder;
    private SessionRegistry sessions;


    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Class<? extends ServerSocketChannel> channelClass;

}
