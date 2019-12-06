package io.jmqtt;

import io.netty.channel.socket.SocketChannel;

/**
 * Created by wangkun23 on 2019/12/6.
 */
public abstract class PipelineInitializer {
    public abstract void init(SocketChannel channel) throws Exception;
}
