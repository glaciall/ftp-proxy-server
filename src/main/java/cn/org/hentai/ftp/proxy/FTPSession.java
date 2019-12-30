package cn.org.hentai.ftp.proxy;

import cn.org.hentai.ftp.interceptor.AbstractSessionInterceptor;
import cn.org.hentai.ftp.interceptor.PassiveProxyInterceptor;
import cn.org.hentai.ftp.util.Configs;
import cn.org.hentai.ftp.util.Packet;
import io.netty.channel.ChannelHandlerContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Created by matrixy on 2019/12/26.
 */
public class FTPSession extends Thread
{
    Object lock = null;
    Socket conn = null;
    ChannelHandlerContext client;
    LinkedList<Packet> packets;

    static Class<? extends AbstractSessionInterceptor> defaultSessionInterceptor = null;

    public static void setDefaultSessionInterceptor(Class<? extends AbstractSessionInterceptor> defaultSessionInterceptor)
    {
        FTPSession.defaultSessionInterceptor = defaultSessionInterceptor;
    }

    public FTPSession(ChannelHandlerContext client)
    {
        this.lock = new Object();
        this.packets = new LinkedList<>();
        this.client = client;
    }

    public void send(Packet p)
    {
        synchronized (lock)
        {
            this.packets.addLast(p);
            lock.notify();
        }
    }

    private AbstractSessionInterceptor getSessionInterceptor()
    {
        try
        {
            return (AbstractSessionInterceptor) defaultSessionInterceptor.newInstance();
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void run()
    {
        AbstractSessionInterceptor interceptor = getSessionInterceptor();

        while (!this.isInterrupted())
        {
            try
            {
                conn = new Socket();
                conn.connect(new InetSocketAddress(Configs.get("ftp.server.addr"), Configs.getInt("ftp.server.port", 21)), 1000);
                conn.setSoTimeout(Configs.getInt("ftp.io.timeout", 5000));

                InputStream input = conn.getInputStream();
                OutputStream output = conn.getOutputStream();

                Packet packet = null;
                byte[] block = new byte[1024];

                while (true)
                {
                    // 等待上游消息
                    int len = 0;
                    while (true)
                    {
                        int buffLength = input.available();
                        if (buffLength == 0) break;
                        len = input.read(block, 0, Math.min(block.length, buffLength));
                        if (len > 0)
                        {
                            byte[] d = interceptor.onUpstreamData(block, len);
                            if (d != null) client.writeAndFlush(d).await();
                        }
                    }

                    // 等待下游消息
                    packet = null;
                    synchronized (lock)
                    {
                        if (this.packets.isEmpty())
                        {
                            lock.wait(10);
                        }

                        if (!this.packets.isEmpty())
                        {
                            packet = this.packets.removeFirst();
                        }
                    }

                    // 发送到上游
                    if (packet != null)
                    {
                        byte[] d = interceptor.onClientData(packet.getBytes(), packet.size());
                        if (d != null)
                        {
                            output.write(d);
                            output.flush();
                        }
                    }
                }
            }
            catch(Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    public void close()
    {
        this.interrupt();
        try { this.conn.close(); } catch(Exception e) { }
    }
}
