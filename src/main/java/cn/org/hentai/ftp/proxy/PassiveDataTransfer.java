package cn.org.hentai.ftp.proxy;

import cn.org.hentai.ftp.interceptor.AbstractSessionInterceptor;
import cn.org.hentai.ftp.interceptor.SimpleSessionInterceptor;
import cn.org.hentai.ftp.util.Configs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by matrixy on 2019/12/28.
 */
public class PassiveDataTransfer extends Thread
{
    static Logger logger = LoggerFactory.getLogger(PassiveDataTransfer.class);
    static final AtomicLong sequence = new AtomicLong(0L);

    int upstreamPassivePort;
    int localPassivePort;
    SimpleSessionInterceptor interceptor;

    // 本地连接监听
    ServerSocket localPassiveServerSocket;

    public PassiveDataTransfer(SimpleSessionInterceptor interceptor, int upstreamPassivePort)
    {
        this.interceptor = interceptor;
        this.upstreamPassivePort = upstreamPassivePort;
        this.localPassivePort = -1;

        this.setName("passive-data-transfer-" + sequence.addAndGet(1L));
    }

    // 分配Passive端口，并且完成绑定
    public int allocatePort()
    {
        if (localPassivePort == -1)
        {
            String passivePort = Configs.get("ftp.proxy.passive.port");
            if (passivePort == null || passivePort.matches("^(\\d+)\\-(\\d+)$") == false) throw new RuntimeException("no passive port range defined");
            int min = Integer.parseInt(passivePort.replaceAll("^(\\d+)\\-(\\d+)$", "$1"));
            int max = Integer.parseInt(passivePort.replaceAll("^(\\d+)\\-(\\d+)$", "$2"));
            if (min < 1024 || min > 65535) throw new RuntimeException("invalid passive port: " + min);
            if (max < min || max > 65535) throw new RuntimeException("invalid passive port: " + max);

            System.err.println("Port Range: " + min + " => " + max);

            try { localPassiveServerSocket = new ServerSocket(); } catch(Exception ex) { throw new RuntimeException(ex); }
            // 尝试绑定100次，再不成功就直接退出了
            boolean bound = false;
            for (int i = 0; i < 100; i++)
            {
                int port = (int)(min + Math.random() * (max - min));
                try
                {
                    localPassiveServerSocket.bind(new InetSocketAddress("0.0.0.0", port), 1);
                    bound = true;
                    localPassivePort = port;
                    break;
                }
                catch(Exception ex) { }
            }
            if (!bound) throw new RuntimeException("passive port allocate failed after 100 times retries");
        }
        return localPassivePort;
    }

    public void run()
    {
        int len = 0;
        byte[] buff = new byte[1024];

        InputStream cis = null, uis = null;
        OutputStream cos = null, uos = null;

        Socket conn = null, upstream = null;
        try
        {
            upstream = new Socket(Configs.get("ftp.server.addr"), upstreamPassivePort);
            uis = upstream.getInputStream();
            uos = upstream.getOutputStream();

            logger.info("connected to upstream passive port...");

            // 设定accept()等待连接的超时时长
            localPassiveServerSocket.setSoTimeout(5000);

            conn = localPassiveServerSocket.accept();
            cis = conn.getInputStream();
            cos = conn.getOutputStream();

            logger.info("client connected...");

            while (!this.isInterrupted())
            {
                boolean available = false;
                if (cis.available() > 0)
                {
                    while (true)
                    {
                        len = cis.read(buff, 0, 1024);
                        if (len == -1) break;
                        uos.write(buff, 0, len);
                        interceptor.onPassiveRequest(buff, len);
                        uos.flush();
                    }
                    available = true;
                    break;
                }
                if (uis.available() > 0)
                {
                    while (true)
                    {
                        len = uis.read(buff, 0, 1024);
                        if (len == -1) break;
                        cos.write(buff, 0, len);
                        interceptor.onPassiveResponse(buff, len);
                        cos.flush();
                    }

                    available = true;
                    break;
                }
                if (available) continue;
                Thread.sleep(5);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try { conn.close(); } catch(Exception e) { }
            try { upstream.close(); } catch(Exception e) { }
            try { localPassiveServerSocket.close(); } catch(Exception e) { }
        }
    }

    public void close()
    {
        this.interrupt();
    }
}
