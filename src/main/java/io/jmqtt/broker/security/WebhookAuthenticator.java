package io.jmqtt.broker.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义实现webhook调用验证用户数据
 * Created by wangkun23 on 2019/12/3.
 */
public class WebhookAuthenticator implements IAuthenticator {
    final Logger LOG = LoggerFactory.getLogger(ResourceAuthenticator.class);

    /**
     * 只验证规则
     *
     * @param clientId
     * @param username
     * @param password
     * @return
     */
    @Override
    public boolean checkValid(String clientId, String username, byte[] password) {
        if (username == null || password == null) {
            LOG.info("username or password was null");
            return false;
        }

        return true;
    }
}
