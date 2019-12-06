package io.jmqtt.acceptor;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * Created by wangkun23 on 2019/12/6.
 */
public class ByteBufToWebSocketFrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext chc, ByteBuf bb, List<Object> out) throws Exception {
        // convert the ByteBuf to a WebSocketFrame
        BinaryWebSocketFrame result = new BinaryWebSocketFrame();
        // System.out.println("ByteBufToWebSocketFrameEncoder encode - " +
        // ByteBufUtil.hexDump(bb));
        result.content().writeBytes(bb);
        out.add(result);
    }
}
