/*
 * Copyright (c) 2012-2018 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.jmqtt;

import io.jmqtt.broker.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

import java.io.IOException;
import java.io.StringWriter;

/**
 * MainServer启动类
 * Created by wangkun23 on 2019/12/02.
 */
@SpringBootApplication
public class MainServer implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(MainServer.class);

    //@Resource
    //private NewNettyAcceptor newNettyAcceptor;

    /**
     * main
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {
        SpringApplication.run(MainServer.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //LaunchOptions res = parseArguments(args);
        //if (res.isHelpNeeded()) {
        //   printUsage(res);
        // Help was requested, so we are done here
        //   return;
        // }
        // if (res.getOption().equalsIgnoreCase("start")) {
       // newNettyAcceptor.initialize();
        // Bind a shutdown hook
        // Runtime.getRuntime().addShutdownHook(new Thread(newNettyAcceptor::close));
        // }
    }

    private LaunchOptions parseArguments(String[] args) {
        LaunchOptions res = new LaunchOptions();
        CmdLineParser parser = new CmdLineParser(res);
        try {
            parser.parseArgument(args);
        } catch (final CmdLineException e) {
            printUsage(res);
        }
        return res;
    }

    private void printUsage(Object options) {
        CmdLineParser parser = new CmdLineParser(options);
        StringWriter sw = new StringWriter();
        sw.append("Usage: jmqtt \n");
        sw.append("   or  jmqtt <source> <destination>\n");
        sw.append("   or  jmqtt [OPTION]... [<value>...]\n\n");
        sw.append("Options:");
        logger.info("{}", sw.toString());
        parser.getProperties().withUsageWidth(100);
        parser.printUsage(System.out);
    }

    /**
     * Use the broker to publish a message. It's intended for embedding applications. It can be used
     * only after the integration is correctly started with startServer.
     *
     * @param msg      the message to forward.
     * @param clientId the id of the sending integration.
     * @throws IllegalStateException if the integration is not yet started
     */
//    public void internalPublish(MqttPublishMessage msg, final String clientId) {
//        final int messageID = msg.variableHeader().packetId();
//        if (!initialized) {
//            logger.error("Moquette is not started, internal message cannot be published. CId: {}, messageId: {}", clientId,
//                    messageID);
//            throw new IllegalStateException("Can't publish on a integration is not yet started");
//        }
//        logger.trace("Internal publishing message CId: {}, messageId: {}", clientId, messageID);
//        dispatcher.internalPublish(msg);
//    }

//    public void stopServer() {
//        logger.info("Unbinding integration from the configured ports");
//        acceptor.close();
//        logger.trace("Stopping MQTT protocol processor");
//        initialized = false;
//
//        // calling shutdown() does not actually stop tasks that are not cancelled,
//        // and SessionsRepository does not stop its tasks. Thus shutdownNow().
//        scheduler.shutdownNow();
//
//        if (h2Builder != null) {
//            logger.trace("Shutting down H2 persistence {}");
//            h2Builder.closeStore();
//        }
//
//        logger.info("Moquette integration has been stopped.");
//    }

    /**
     * SPI method used by Broker embedded applications to get list of subscribers. Returns null if
     * the broker is not started.
     *
     * @return list of subscriptions.
     */
// TODO reimplement this
//    public List<Subscription> getSubscriptions() {
//        if (m_processorBootstrapper == null) {
//            return null;
//        }
//        return this.subscriptionsStore.listAllSubscriptions();
//    }

    /**
     * SPI method used by Broker embedded applications to add intercept handlers.
     *
     * @param interceptHandler the handler to add.
     */
//    public void addInterceptHandler(InterceptHandler interceptHandler) {
//        if (!initialized) {
//            logger.error("Moquette is not started, MQTT message interceptor cannot be added. InterceptorId={}",
//                    interceptHandler.getID());
//            throw new IllegalStateException("Can't register interceptors on a integration that is not yet started");
//        }
//        logger.info("Adding MQTT message interceptor. InterceptorId={}", interceptHandler.getID());
//        interceptor.addInterceptHandler(interceptHandler);
//    }

    /**
     * SPI method used by Broker embedded applications to remove intercept handlers.
     *
     * @param interceptHandler the handler to remove.
     */
//    public void removeInterceptHandler(InterceptHandler interceptHandler) {
//        if (!initialized) {
//            logger.error("Moquette is not started, MQTT message interceptor cannot be removed. InterceptorId={}",
//                    interceptHandler.getID());
//            throw new IllegalStateException("Can't deregister interceptors from a integration that is not yet started");
//        }
//        logger.info("Removing MQTT message interceptor. InterceptorId={}", interceptHandler.getID());
//        interceptor.removeInterceptHandler(interceptHandler);
//    }

    /**
     * Return a list of descriptors of connected clients.
     */
//    public Collection<ClientDescriptor> listConnectedClients() {
//        return sessions.listConnectedClients();
//    }
}
