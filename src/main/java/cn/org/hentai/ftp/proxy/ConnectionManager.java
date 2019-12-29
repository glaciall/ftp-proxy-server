package cn.org.hentai.ftp.proxy;

import cn.org.hentai.ftp.util.Configs;
import cn.org.hentai.ftp.util.Packet;
import io.netty.channel.ChannelHandlerContext;

import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by matrixy on 2019/12/24.
 */
public final class ConnectionManager
{
    static final AtomicLong sequence = new AtomicLong(0L);

    ConcurrentHashMap<Long, FTPSession> connections;

    private ConnectionManager()
    {
        this.connections = new ConcurrentHashMap<Long, FTPSession>();
    }

    // 申请一个新的连接
    public long request(ChannelHandlerContext ctx)
    {
        long nid = sequence.addAndGet(1L);
        FTPSession session = new FTPSession(ctx);
        session.setName("ftp-session-" + nid);
        this.connections.put(nid, session);
        session.start();
        return nid;
    }

    public void send(Long connId, Packet msg)
    {
        FTPSession session = this.connections.get(connId);
        if (session != null) session.send(msg);
    }

    public void close(Long connId)
    {
        if (connId == null) return;
        FTPSession session = this.connections.get(connId);
        if (session != null) session.close();
    }

    public static final ConnectionManager instance = new ConnectionManager();
    public static void init()
    {
        // ..
    }

    public static ConnectionManager getInstance()
    {
        return instance;
    }
}
