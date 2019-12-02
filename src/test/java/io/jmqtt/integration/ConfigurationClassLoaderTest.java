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

package io.jmqtt.integration;

import io.jmqtt.BrokerConstants;
import io.jmqtt.MainServer;
import io.jmqtt.broker.config.IConfig;
import io.jmqtt.broker.config.MemoryConfig;
import io.jmqtt.broker.security.IAuthenticator;
import io.jmqtt.broker.security.IAuthorizatorPolicy;
import io.jmqtt.broker.subscriptions.Topic;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class ConfigurationClassLoaderTest implements IAuthenticator, IAuthorizatorPolicy {

    MainServer m_Main_server;
    IConfig m_config;

    protected void startServer(Properties props) throws IOException {
        m_Main_server = new MainServer();
        m_config = new MemoryConfig(props);
        m_Main_server.startServer(m_config);
    }

    @After
    public void tearDown() {
        m_Main_server.stopServer();
    }

    @Test
    public void loadAuthenticator() throws Exception {
        Properties props = new Properties(IntegrationUtils.prepareTestProperties());
        props.setProperty(BrokerConstants.AUTHENTICATOR_CLASS_NAME, getClass().getName());
        startServer(props);
        assertTrue(true);
    }

    @Test
    public void loadAuthorizator() throws Exception {
        Properties props = new Properties(IntegrationUtils.prepareTestProperties());
        props.setProperty(BrokerConstants.AUTHORIZATOR_CLASS_NAME, getClass().getName());
        startServer(props);
        assertTrue(true);
    }

    @Override
    public boolean checkValid(String clientID, String username, byte[] password) {
        return true;
    }

    @Override
    public boolean canWrite(Topic topic, String user, String client) {
        return true;
    }

    @Override
    public boolean canRead(Topic topic, String user, String client) {
        return true;
    }

}
