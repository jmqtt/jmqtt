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
import io.jmqtt.broker.config.*;
import io.jmqtt.broker.security.*;
import io.jmqtt.interception.InterceptHandler;
import io.jmqtt.persistence.H2Builder;
import io.jmqtt.persistence.MemorySubscriptionsRepository;
import io.jmqtt.interception.BrokerInterceptor;
import io.jmqtt.broker.subscriptions.CTrieSubscriptionDirectory;
import io.jmqtt.broker.subscriptions.ISubscriptionsDirectory;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static io.jmqtt.logging.LoggingUtils.getInterceptorIds;

/**
 * MainServer启动类
 * Created by wangkun23 on 2019/12/02.
 */
@SpringBootApplication
public class MainServer implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(MainServer.class);

    private ScheduledExecutorService scheduler;
    private NewNettyAcceptor acceptor;
    private volatile boolean initialized;
    private PostOffice dispatcher;
    private BrokerInterceptor interceptor;
    private H2Builder h2Builder;
    private SessionRegistry sessions;

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
            final MainServer mainServer = new MainServer();
            // 用测试文件
            IResourceLoader classpathLoader = new ClasspathResourceLoader();
            final IConfig classPathConfig = new ResourceLoaderConfig(classpathLoader);
            mainServer.startServer(classPathConfig);
            System.out.println("Server started, version 0.13-SNAPSHOT");
            //Bind a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(mainServer::stopServer));
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
     * Starts Moquette bringing the configuration from the file located at m_config/moquette.conf
     *
     * @throws IOException in case of any IO error.
     */
    public void startServer() throws IOException {
        File defaultConfigurationFile = defaultConfigFile();
        logger.info("Starting Moquette integration. Configuration file path={}", defaultConfigurationFile.getAbsolutePath());
        IResourceLoader filesystemLoader = new FileResourceLoader(defaultConfigurationFile);
        final IConfig config = new ResourceLoaderConfig(filesystemLoader);
        startServer(config);
    }

    private static File defaultConfigFile() {
        String configPath = System.getProperty("moquette.path", null);
        return new File(configPath, IConfig.DEFAULT_CONFIG);
    }

    /**
     * Starts Moquette bringing the configuration from the given file
     *
     * @param configFile text file that contains the configuration.
     * @throws IOException in case of any IO Error.
     */
    public void startServer(File configFile) throws IOException {
        logger.info("Starting Moquette integration. Configuration file path: {}", configFile.getAbsolutePath());
        IResourceLoader filesystemLoader = new FileResourceLoader(configFile);
        final IConfig config = new ResourceLoaderConfig(filesystemLoader);
        startServer(config);
    }

    /**
     * Starts the integration with the given properties.
     * <p>
     * Its suggested to at least have the following properties:
     * <ul>
     * <li>port</li>
     * <li>password_file</li>
     * </ul>
     *
     * @param configProps the properties map to use as configuration.
     * @throws IOException in case of any IO Error.
     */
    public void startServer(Properties configProps) throws IOException {
        logger.debug("Starting Moquette integration using properties object");
        final IConfig config = new MemoryConfig(configProps);
        startServer(config);
    }

    /**
     * Starts Moquette bringing the configuration files from the given Config implementation.
     *
     * @param config the configuration to use to start the broker.
     * @throws IOException in case of any IO Error.
     */
    public void startServer(IConfig config) throws IOException {
        logger.debug("Starting Moquette integration using IConfig instance");
        startServer(config, null);
    }

    /**
     * Starts Moquette with config provided by an implementation of IConfig class and with the set
     * of InterceptHandler.
     *
     * @param config   the configuration to use to start the broker.
     * @param handlers the handlers to install in the broker.
     * @throws IOException in case of any IO Error.
     */
    public void startServer(IConfig config, List<? extends InterceptHandler> handlers) throws IOException {
        logger.debug("Starting moquette integration using IConfig instance and intercept handlers");
        startServer(config, handlers, null, null, null);
    }

    public void startServer(IConfig config, List<? extends InterceptHandler> handlers, ISslContextCreator sslCtxCreator,
                            IAuthenticator authenticator, IAuthorizatorPolicy authorizatorPolicy) {
        final long start = System.currentTimeMillis();
        if (handlers == null) {
            handlers = Collections.emptyList();
        }
        logger.trace("Starting Moquette Server. MQTT message interceptors={}", getInterceptorIds(handlers));

        scheduler = Executors.newScheduledThreadPool(1);

        final String handlerProp = System.getProperty(BrokerConstants.INTERCEPT_HANDLER_PROPERTY_NAME);
        if (handlerProp != null) {
            config.setProperty(BrokerConstants.INTERCEPT_HANDLER_PROPERTY_NAME, handlerProp);
        }
        final String persistencePath = config.getProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME);
        logger.debug("Configuring Using persistent store file, path: {}", persistencePath);
        initInterceptors(config, handlers);
        logger.debug("Initialized MQTT protocol processor");
        if (sslCtxCreator == null) {
            logger.info("Using default SSL context creator");
            sslCtxCreator = new DefaultMoquetteSslContextCreator(config);
        }
        authenticator = initializeAuthenticator(authenticator, config);
        authorizatorPolicy = initializeAuthorizatorPolicy(authorizatorPolicy, config);

        final ISubscriptionsRepository subscriptionsRepository;
        final IQueueRepository queueRepository;
        final IRetainedRepository retainedRepository;
        if (persistencePath != null && !persistencePath.isEmpty()) {
            logger.trace("Configuring H2 subscriptions store to {}", persistencePath);
            h2Builder = new H2Builder(config, scheduler).initStore();
            subscriptionsRepository = h2Builder.subscriptionsRepository();
            queueRepository = h2Builder.queueRepository();
            retainedRepository = h2Builder.retainedRepository();
        } else {
            logger.trace("Configuring in-memory subscriptions store");
            subscriptionsRepository = new MemorySubscriptionsRepository();
            queueRepository = new MemoryQueueRepository();
            retainedRepository = new MemoryRetainedRepository();
        }

        ISubscriptionsDirectory subscriptions = new CTrieSubscriptionDirectory();
        subscriptions.init(subscriptionsRepository);
        final Authorizator authorizator = new Authorizator(authorizatorPolicy);
        sessions = new SessionRegistry(subscriptions, queueRepository, authorizator);
        dispatcher = new PostOffice(subscriptions, retainedRepository, sessions, interceptor, authorizator);
        final BrokerConfiguration brokerConfig = new BrokerConfiguration(config);
        MQTTConnectionFactory connectionFactory = new MQTTConnectionFactory(brokerConfig, authenticator, sessions,
                dispatcher);

        final NewNettyMQTTHandler mqttHandler = new NewNettyMQTTHandler(connectionFactory);
        acceptor = new NewNettyAcceptor();
        acceptor.initialize(mqttHandler, config, sslCtxCreator);

        final long startTime = System.currentTimeMillis() - start;
        logger.info("Moquette integration has been started successfully in {} ms", startTime);
        initialized = true;
    }

    private IAuthorizatorPolicy initializeAuthorizatorPolicy(IAuthorizatorPolicy authorizatorPolicy, IConfig props) {
        logger.debug("Configuring MQTT authorizator policy");
        String authorizatorClassName = props.getProperty(BrokerConstants.AUTHORIZATOR_CLASS_NAME, "");
        if (authorizatorPolicy == null && !authorizatorClassName.isEmpty()) {
            authorizatorPolicy = loadClass(authorizatorClassName, IAuthorizatorPolicy.class, IConfig.class, props);
        }

        if (authorizatorPolicy == null) {
            String aclFilePath = props.getProperty(BrokerConstants.ACL_FILE_PROPERTY_NAME, "");
            if (aclFilePath != null && !aclFilePath.isEmpty()) {
                authorizatorPolicy = new DenyAllAuthorizatorPolicy();
                try {
                    logger.info("Parsing ACL file. Path = {}", aclFilePath);
                    IResourceLoader resourceLoader = props.getResourceLoader();
                    authorizatorPolicy = ACLFileParser.parse(resourceLoader.loadResource(aclFilePath));
                } catch (ParseException pex) {
                    logger.error("Unable to parse ACL file. path = {}", aclFilePath, pex);
                }
            } else {
                authorizatorPolicy = new PermitAllAuthorizatorPolicy();
            }
            logger.info("Authorizator policy {} instance will be used", authorizatorPolicy.getClass().getName());
        }
        return authorizatorPolicy;
    }

    private IAuthenticator initializeAuthenticator(IAuthenticator authenticator, IConfig props) {
        logger.debug("Configuring MQTT authenticator");
        String authenticatorClassName = props.getProperty(BrokerConstants.AUTHENTICATOR_CLASS_NAME, "");

        if (authenticator == null && !authenticatorClassName.isEmpty()) {
            authenticator = loadClass(authenticatorClassName, IAuthenticator.class, IConfig.class, props);
        }

        IResourceLoader resourceLoader = props.getResourceLoader();
        if (authenticator == null) {
            String passwdPath = props.getProperty(BrokerConstants.PASSWORD_FILE_PROPERTY_NAME, "");
            if (passwdPath.isEmpty()) {
                authenticator = new AcceptAllAuthenticator();
            } else {
                authenticator = new ResourceAuthenticator(resourceLoader, passwdPath);
            }
            logger.info("An {} authenticator instance will be used", authenticator.getClass().getName());
        }
        return authenticator;
    }

    private void initInterceptors(IConfig props, List<? extends InterceptHandler> embeddedObservers) {
        logger.info("Configuring message interceptors...");

        List<InterceptHandler> observers = new ArrayList<>(embeddedObservers);
        String interceptorClassName = props.getProperty(BrokerConstants.INTERCEPT_HANDLER_PROPERTY_NAME);
        if (interceptorClassName != null && !interceptorClassName.isEmpty()) {
            InterceptHandler handler = loadClass(interceptorClassName, InterceptHandler.class,
                    MainServer.class, this);
            if (handler != null) {
                observers.add(handler);
            }
        }
        interceptor = new BrokerInterceptor(props, observers);
    }

    @SuppressWarnings("unchecked")
    private <T, U> T loadClass(String className, Class<T> intrface, Class<U> constructorArgClass, U props) {
        T instance = null;
        try {
            // check if constructor with constructor arg class parameter
            // exists
            logger.info("Invoking constructor with {} argument. ClassName={}, interfaceName={}",
                    constructorArgClass.getName(), className, intrface.getName());
            instance = this.getClass().getClassLoader()
                    .loadClass(className)
                    .asSubclass(intrface)
                    .getConstructor(constructorArgClass)
                    .newInstance(props);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            logger.warn("Unable to invoke constructor with {} argument. ClassName={}, interfaceName={}, cause={}, " +
                            "errorMessage={}", constructorArgClass.getName(), className, intrface.getName(), ex.getCause(),
                    ex.getMessage());
            return null;
        } catch (NoSuchMethodException | InvocationTargetException e) {
            try {
                logger.info("Invoking default constructor. ClassName={}, interfaceName={}", className, intrface.getName());
                // fallback to default constructor
                instance = this.getClass().getClassLoader()
                        .loadClass(className)
                        .asSubclass(intrface)
                        .getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException |
                    NoSuchMethodException | InvocationTargetException ex) {
                logger.error("Unable to invoke default constructor. ClassName={}, interfaceName={}, cause={}, " +
                        "errorMessage={}", className, intrface.getName(), ex.getCause(), ex.getMessage());
                return null;
            }
        }

        return instance;
    }

    /**
     * Use the broker to publish a message. It's intended for embedding applications. It can be used
     * only after the integration is correctly started with startServer.
     *
     * @param msg      the message to forward.
     * @param clientId the id of the sending integration.
     * @throws IllegalStateException if the integration is not yet started
     */
    public void internalPublish(MqttPublishMessage msg, final String clientId) {
        final int messageID = msg.variableHeader().packetId();
        if (!initialized) {
            logger.error("Moquette is not started, internal message cannot be published. CId: {}, messageId: {}", clientId,
                    messageID);
            throw new IllegalStateException("Can't publish on a integration is not yet started");
        }
        logger.trace("Internal publishing message CId: {}, messageId: {}", clientId, messageID);
        dispatcher.internalPublish(msg);
    }

    public void stopServer() {
        logger.info("Unbinding integration from the configured ports");
        acceptor.close();
        logger.trace("Stopping MQTT protocol processor");
        initialized = false;

        // calling shutdown() does not actually stop tasks that are not cancelled,
        // and SessionsRepository does not stop its tasks. Thus shutdownNow().
        scheduler.shutdownNow();

        if (h2Builder != null) {
            logger.trace("Shutting down H2 persistence {}");
            h2Builder.closeStore();
        }

        logger.info("Moquette integration has been stopped.");
    }

    public int getPort() {
        return acceptor.getPort();
    }

    public int getSslPort() {
        return acceptor.getSslPort();
    }

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
    public void addInterceptHandler(InterceptHandler interceptHandler) {
        if (!initialized) {
            logger.error("Moquette is not started, MQTT message interceptor cannot be added. InterceptorId={}",
                    interceptHandler.getID());
            throw new IllegalStateException("Can't register interceptors on a integration that is not yet started");
        }
        logger.info("Adding MQTT message interceptor. InterceptorId={}", interceptHandler.getID());
        interceptor.addInterceptHandler(interceptHandler);
    }

    /**
     * SPI method used by Broker embedded applications to remove intercept handlers.
     *
     * @param interceptHandler the handler to remove.
     */
    public void removeInterceptHandler(InterceptHandler interceptHandler) {
        if (!initialized) {
            logger.error("Moquette is not started, MQTT message interceptor cannot be removed. InterceptorId={}",
                    interceptHandler.getID());
            throw new IllegalStateException("Can't deregister interceptors from a integration that is not yet started");
        }
        logger.info("Removing MQTT message interceptor. InterceptorId={}", interceptHandler.getID());
        interceptor.removeInterceptHandler(interceptHandler);
    }

    /**
     * Return a list of descriptors of connected clients.
     */
    public Collection<ClientDescriptor> listConnectedClients() {
        return sessions.listConnectedClients();
    }
}
