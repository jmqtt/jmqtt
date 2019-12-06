package io.jmqtt.config;

import io.jmqtt.BrokerConstants;
import io.jmqtt.broker.*;
import io.jmqtt.broker.config.IConfig;
import io.jmqtt.broker.config.IResourceLoader;
import io.jmqtt.broker.security.*;
import io.jmqtt.broker.subscriptions.CTrieSubscriptionDirectory;
import io.jmqtt.broker.subscriptions.ISubscriptionsDirectory;
import io.jmqtt.interception.BrokerInterceptor;
import io.jmqtt.interception.InterceptHandler;
import io.jmqtt.persistence.H2Builder;
import io.jmqtt.persistence.MemorySubscriptionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static io.jmqtt.logging.LoggingUtils.getInterceptorIds;

/**
 * Created by wangkun23 on 2019/12/6.
 */
@Configuration
public class NewNettyMQTTHandlerConfig {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private PostOffice dispatcher;
    private BrokerInterceptor interceptor;
    private H2Builder h2Builder;
    private SessionRegistry sessions;

    @Resource
    private IConfig config;

    @Bean
    public NewNettyMQTTHandler newNettyMQTTHandler() {
        return new NewNettyMQTTHandler(mqttConnectionFactory());
    }

    /**
     * mqttConnectionFactory
     *
     * @return
     */
    @Bean
    public MQTTConnectionFactory mqttConnectionFactory() {
        List<? extends InterceptHandler> handlers = Collections.emptyList();
        // TODO.. 如果需要增加处理 如果数据同步 集群等..

        logger.trace("Starting Moquette Server. MQTT message interceptors={}", getInterceptorIds(handlers));

        final String handlerProp = System.getProperty(BrokerConstants.INTERCEPT_HANDLER_PROPERTY_NAME);
        if (handlerProp != null) {
            config.setProperty(BrokerConstants.INTERCEPT_HANDLER_PROPERTY_NAME, handlerProp);
        }
        final String persistencePath = config.getProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME);
        logger.debug("Configuring Using persistent store file, path: {}", persistencePath);
        initInterceptors(config, handlers);
        logger.debug("Initialized MQTT protocol processor");
        IAuthenticator authenticator = initializeAuthenticator(config);
        IAuthorizatorPolicy authorizatorPolicy = initializeAuthorizatorPolicy(config);

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
        return new MQTTConnectionFactory(brokerConfig, authenticator, sessions, dispatcher);
    }

    private IAuthorizatorPolicy initializeAuthorizatorPolicy(IConfig props) {
        logger.debug("Configuring MQTT authorizator policy");
        IAuthorizatorPolicy authorizatorPolicy;
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
        return authorizatorPolicy;
    }

    private IAuthenticator initializeAuthenticator(IConfig props) {
        logger.debug("Configuring MQTT authenticator");
        IResourceLoader resourceLoader = props.getResourceLoader();
        String passwdPath = props.getProperty(BrokerConstants.PASSWORD_FILE_PROPERTY_NAME, "");
        IAuthenticator authenticator;
        if (passwdPath.isEmpty()) {
            authenticator = new AcceptAllAuthenticator();
        } else {
            authenticator = new ResourceAuthenticator(resourceLoader, passwdPath);
        }
        logger.info("An {} authenticator instance will be used", authenticator.getClass().getName());
        return authenticator;
    }

    private void initInterceptors(IConfig props, List<? extends InterceptHandler> embeddedObservers) {
        logger.info("Configuring message interceptors...");
        List<InterceptHandler> observers = new ArrayList<>(embeddedObservers);
//        String interceptorClassName = props.getProperty(BrokerConstants.INTERCEPT_HANDLER_PROPERTY_NAME);
//        if (interceptorClassName != null && !interceptorClassName.isEmpty()) {
//            InterceptHandler handler = loadClass(interceptorClassName, InterceptHandler.class,
//                    MainServer.class, this);
//            if (handler != null) {
//                observers.add(handler);
//            }
//        }
        interceptor = new BrokerInterceptor(props, observers);
    }
}
