package io.jmqtt.acceptor;

import io.jmqtt.broker.NewNettyAcceptor;
import io.jmqtt.config.JmqttProperties;
import io.jmqtt.config.NettyProperties;
import io.jmqtt.handler.MqttTcpChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

/**
 * Created by wangkun23 on 2019/12/12.
 */
@Component
public class MqttTcpAcceptor implements InitializingBean {
    final Logger logger = LoggerFactory.getLogger(getClass());
    public static final String PLAIN_MQTT_PROTO = "TCP MQTT";

    @Resource
    private NettyProperties nettyProperties;

    @Resource
    private JmqttProperties jmqttProperties;

    @Resource
    private EventLoopGroup bossGroup;

    @Resource
    private EventLoopGroup workerGroup;

    @Resource
    private MqttTcpChannelInitializer mqttTcpChannelInitializer;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.debug("Initializing integration. Protocol={}", nettyProperties);
        String protocol = PLAIN_MQTT_PROTO;
        String host = jmqttProperties.getHost();
        int port = jmqttProperties.getPort();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(mqttTcpChannelInitializer)
                .option(ChannelOption.SO_BACKLOG, nettyProperties.getSoBacklog())
                .option(ChannelOption.SO_REUSEADDR, nettyProperties.getSoReuseaddr())
                .childOption(ChannelOption.TCP_NODELAY, nettyProperties.getTcpNodelay())
                .childOption(ChannelOption.SO_KEEPALIVE, nettyProperties.getSoKeepalive());
        try {
            logger.debug("Binding integration. host={}, port={}", jmqttProperties.getHost(), port);
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(host, port);
            logger.info("Server bound to host={}, port={}, protocol={}", host, port, protocol);
            f.sync().addListener(new LocalPortReaderFutureListener(protocol))
                    .addListener(FIRE_EXCEPTION_ON_FAILURE);
        } catch (InterruptedException ex) {
            logger.error("An interruptedException was caught while initializing integration. Protocol={}", protocol, ex);
        }
    }
}
