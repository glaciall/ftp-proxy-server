package cn.org.hentai.ftp.proxy;

import cn.org.hentai.ftp.util.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by matrixy on 2019/12/24.
 */
public class FTPMessageDecoder extends ByteToMessageDecoder
{
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception
    {
        int length = byteBuf.readableBytes();
        if (length <= 0) return;
        byte[] data = new byte[length];
        byteBuf.readBytes(data);
        list.add(Packet.create(data));
    }
}
