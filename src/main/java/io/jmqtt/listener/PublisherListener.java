package io.jmqtt.listener;

import io.jmqtt.interception.AbstractInterceptHandler;
import io.jmqtt.interception.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Created by wangkun23 on 2019/12/2.
 */
public class PublisherListener extends AbstractInterceptHandler {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onConnect(InterceptConnectMessage msg) {
        logger.info("onConnect {}", msg.getClientID());
        logger.info("onConnect getUsername {}", msg.getUsername());
        logger.info("onConnect getPassword {}", new String(msg.getPassword(), StandardCharsets.UTF_8));
    }

    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {
        logger.info("onDisconnect {}", msg.getClientID());
        logger.info("Disconnect getUsername {}", msg.getUsername());
    }

    @Override
    public void onConnectionLost(InterceptConnectionLostMessage msg) {
        logger.info("onConnectionLost {}", msg.getClientID());
        logger.info("ConnectionLost getUsername {}", msg.getUsername());
    }

    @Override
    public void onSubscribe(InterceptSubscribeMessage msg) {
        logger.info("onSubscribe {}", msg.getClientID());
        logger.info("onSubscribe topic {}", msg.getTopicFilter());
    }

    @Override
    public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
        logger.info("onUnsubscribe {}", msg.getClientID());
    }

    @Override
    public void onMessageAcknowledged(InterceptAcknowledgedMessage msg) {
        logger.info("getTopic {}", msg.getTopic());
        logger.info("getUsername {}", msg.getUsername());
        logger.info("getPacketID {}", msg.getPacketID());
    }

    @Override
    public String getID() {
        return "EmbeddedLauncherPublishListener";
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        final String decodedPayload = new String(msg.getPayload().array(), StandardCharsets.UTF_8);
        System.out.println("Received on topic: " + msg.getTopicName() + " content: " + decodedPayload);
    }
}
