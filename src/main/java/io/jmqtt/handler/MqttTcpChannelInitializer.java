package io.jmqtt.handler;

import io.jmqtt.broker.AutoFlushHandler;
import io.jmqtt.broker.MoquetteIdleTimeoutHandler;
import io.jmqtt.broker.NewNettyMQTTHandler;
import io.jmqtt.broker.metrics.*;
import io.jmqtt.config.NettyProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangkun23 on 2019/12/6.
 */
@Component
public class MqttTcpChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Resource
    private NettyProperties nettyProperties;

    @Resource
    private BytesMetricsCollector bytesMetricsCollector;

    @Resource
    private MessageMetricsCollector metricsCollector;

    @Resource
    private NewNettyMQTTHandler newNettyMQTTHandler;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        final MoquetteIdleTimeoutHandler timeoutHandler = new MoquetteIdleTimeoutHandler();
        pipeline.addFirst("idleStateHandler", new IdleStateHandler(nettyProperties.getChannelTimeoutSeconds(), 0, 0));
        pipeline.addAfter("idleStateHandler", "idleEventHandler", timeoutHandler);
        // pipeline.addLast("logger", new LoggingHandler("Netty", LogLevel.ERROR));
        // if (errorsCather.isPresent()) {
        //     pipeline.addLast("bugsnagCatcher", errorsCather.get());
        // }
        pipeline.addFirst("bytemetrics", new BytesMetricsHandler(bytesMetricsCollector));
        pipeline.addLast("autoflush", new AutoFlushHandler(1, TimeUnit.SECONDS));
        pipeline.addLast("decoder", new MqttDecoder(nettyProperties.getMaxBytesInMessage()));
        pipeline.addLast("encoder", MqttEncoder.INSTANCE);
        pipeline.addLast("metrics", new MessageMetricsHandler(metricsCollector));
        pipeline.addLast("messageLogger", new MQTTMessageLogger());
        //if (metrics.isPresent()) {
        //    pipeline.addLast("wizardMetrics", metrics.get());
        //}
        pipeline.addLast("handler", newNettyMQTTHandler);
    }
}
