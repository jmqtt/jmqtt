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

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertFalse;

/**
 * Used to carry integration configurations.
 */
public final class IntegrationUtils {

    static String localH2MvStoreDBPath() {
        String currentDir = System.getProperty("user.dir");
        return currentDir + File.separator + "build" + File.separator + BrokerConstants.DEFAULT_MOQUETTE_STORE_H2_DB_FILENAME;
    }

    public static Properties prepareTestProperties() {
        Properties testProperties = new Properties();
        testProperties.put(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, IntegrationUtils.localH2MvStoreDBPath());
        testProperties.put(BrokerConstants.PORT_PROPERTY_NAME, "1883");
        return testProperties;
    }

    private IntegrationUtils() {
    }

    public static void clearTestStorage() {
        String dbPath = localH2MvStoreDBPath();
        File dbFile = new File(dbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        assertFalse(dbFile.exists());
    }
}
