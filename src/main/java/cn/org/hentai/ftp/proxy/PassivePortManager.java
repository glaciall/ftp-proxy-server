package cn.org.hentai.ftp.proxy;

import cn.org.hentai.ftp.util.Configs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by matrixy on 2019/12/31.
 */
public final class PassivePortManager
{
    static Logger logger = LoggerFactory.getLogger(PassivePortManager.class);
    static ConcurrentLinkedDeque<Integer> ports;

    public static int allocate(ServerSocket server)
    {
        Integer port = null;
        try
        {
            for (int i = 0; i < 100; i++)
            {
                port = ports.removeFirst();
                try
                {
                    server.bind(new InetSocketAddress("0.0.0.0", port), 1);
                }
                catch(Exception e)
                {
                    free(port);
                    continue;
                }
                return port;
            }

            throw new RuntimeException("passive port allocate failed after 100 times retries");
        }
        catch(NoSuchElementException ex)
        {
            throw new RuntimeException("port pool empty");
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    // 释放端口号
    public static void free(int port)
    {
        ports.addLast(port);
    }

    // 初始化
    public static void init()
    {
        String passivePort = Configs.get("ftp.proxy.passive.port");
        if (passivePort == null || passivePort.matches("^(\\d+)\\-(\\d+)$") == false) throw new RuntimeException("no passive port range defined");
        int min = Integer.parseInt(passivePort.replaceAll("^(\\d+)\\-(\\d+)$", "$1"));
        int max = Integer.parseInt(passivePort.replaceAll("^(\\d+)\\-(\\d+)$", "$2"));
        if (min < 1024 || min > 65535) throw new RuntimeException("invalid passive port: " + min);
        if (max < min || max > 65535) throw new RuntimeException("invalid passive port: " + max);

        logger.info("Port Range: {} -> {}", min, max);
        List<Integer> numbers = new ArrayList();

        for (; min <= max; min++)
        {
            numbers.add(min);
        }

        Collections.shuffle(numbers);
        ports = new ConcurrentLinkedDeque<>(numbers);
    }
}
