package cn.org.hentai.ftp.test;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by matrixy on 2019/12/28.
 */
public class ClientTest
{
    public static void main(String[] args) throws Exception
    {
        Socket client = new Socket();
        client.setReuseAddress(true);
        client.bind(new InetSocketAddress("0.0.0.0", 1212));

        client.connect(new InetSocketAddress("192.168.10.2", 1111));
        client.getOutputStream().write("fuck you\r\n".getBytes());
        System.in.read();
        client.close();
    }
}
