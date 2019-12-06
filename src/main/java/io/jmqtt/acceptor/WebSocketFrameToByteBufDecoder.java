package io.jmqtt.acceptor;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * Created by wangkun23 on 2019/12/6.
 */
public class WebSocketFrameToByteBufDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {

    @Override
    protected void decode(ChannelHandlerContext chc, BinaryWebSocketFrame frame, List<Object> out)
            throws Exception {
        // convert the frame to a ByteBuf
        ByteBuf bb = frame.content();
        // System.out.println("WebSocketFrameToByteBufDecoder decode - " +
        // ByteBufUtil.hexDump(bb));
        bb.retain();
        out.add(bb);
    }
}
