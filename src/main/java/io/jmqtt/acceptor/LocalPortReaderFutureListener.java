package io.jmqtt.acceptor;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by wangkun23 on 2019/12/6.
 */
public class LocalPortReaderFutureListener implements ChannelFutureListener {
    private final Logger logger = LoggerFactory.getLogger(LocalPortReaderFutureListener.class);
    private String transportName;

    public LocalPortReaderFutureListener(String transportName) {
        this.transportName = transportName;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            final SocketAddress localAddress = future.channel().localAddress();
            if (localAddress instanceof InetSocketAddress) {
                InetSocketAddress inetAddress = (InetSocketAddress) localAddress;
                logger.debug("bound {} port: {}", transportName, inetAddress.getPort());
            }
        }
    }
}
