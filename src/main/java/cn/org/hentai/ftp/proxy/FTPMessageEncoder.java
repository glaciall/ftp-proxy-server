package cn.org.hentai.ftp.proxy;

import cn.org.hentai.ftp.util.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by matrixy on 2019/12/26.
 */
public class FTPMessageEncoder extends MessageToByteEncoder<byte[]>
{
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, byte[] packet, ByteBuf byteBuf) throws Exception
    {
        byteBuf.writeBytes(packet);
    }
}
