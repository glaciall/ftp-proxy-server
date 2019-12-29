package cn.org.hentai.ftp.proxy;

import cn.org.hentai.ftp.util.Packet;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by matrixy on 2019/4/9.
 */
public class FTPHandler extends SimpleChannelInboundHandler<Packet>
{
    static Logger logger = LoggerFactory.getLogger(FTPHandler.class);
    private static final AttributeKey<SessionAttributeSet> SESSION_KEY = AttributeKey.valueOf("session-key");
    private ChannelHandlerContext context;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception
    {
        ConnectionManager mgr = ConnectionManager.getInstance();
        Long connId = getAttributeSet(ctx).get("connection-id");
        mgr.send(connId, packet);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception
    {
        super.channelRegistered(ctx);
        logger.info("Connected: {}", ctx.channel().remoteAddress());
        Long connId = ConnectionManager.getInstance().request(ctx);
        getAttributeSet(ctx).set("connection-id", connId);
    }

    public final SessionAttributeSet getAttributeSet(ChannelHandlerContext ctx)
    {
        Attribute<SessionAttributeSet> attr = ctx.channel().attr(SESSION_KEY);
        if (null == attr || attr.get() == null)
        {
            ctx.channel().attr(SESSION_KEY).set(new SessionAttributeSet());
            attr = ctx.channel().attr(SESSION_KEY);
        }
        return attr.get();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelInactive(ctx);
        release(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        cause.printStackTrace();
        release(ctx);
        ctx.close();
    }

    private void release(ChannelHandlerContext ctx)
    {
        logger.info("Disconnected: {}", ctx.channel().remoteAddress());
        Long connId = getAttributeSet(ctx).get("connection-id");
        ConnectionManager.getInstance().close(connId);
        ctx.close();
    }
}
