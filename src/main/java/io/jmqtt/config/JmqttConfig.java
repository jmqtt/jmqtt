package io.jmqtt.config;

import io.jmqtt.broker.config.ClasspathResourceLoader;
import io.jmqtt.broker.config.IConfig;
import io.jmqtt.broker.config.IResourceLoader;
import io.jmqtt.broker.config.ResourceLoaderConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wangkun23 on 2019/12/6.
 */
@Configuration
public class JmqttConfig {

    /**
     * 加载配置文件
     *
     * @return
     */
    @Bean
    public IConfig iConfig() {
        IResourceLoader classpathLoader = new ClasspathResourceLoader();
        return new ResourceLoaderConfig(classpathLoader);
    }
}
