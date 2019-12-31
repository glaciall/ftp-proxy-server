package cn.org.hentai.ftp.interceptor;

import cn.org.hentai.ftp.message.Message;
import cn.org.hentai.ftp.util.ByteUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by matrixy on 2019/12/31.
 * 一个简单的代理数据拦截器实现，实现对上传文件的已上传大小进行计数
 */
public class TestSessionInterceptor extends PassiveProxyInterceptor
{
    String lastUploadFile = null;
    AtomicLong totalBytes = new AtomicLong(0);

    // 当收到下游FTP客户端发来的数据时触发
    @Override
    public byte[] onClientData(byte[] data, int len)
    {
        return super.onClientData(data, len);
    }

    // 当收到上游FTP服务器响应的数据时触发
    @Override
    public byte[] onUpstreamData(byte[] data, int len)
    {
        return super.onUpstreamData(data, len);
    }

    // 当收到一整行的下游FTP客户端的指令消息时触发
    @Override
    public Message onRequest(Message message)
    {
        String text = message.getText();
        if (text == null || text.length() == 0) throw new RuntimeException("WTF???");

        System.out.println("Response: ");
        System.out.println(text.trim());
        ByteUtils.dump(message.getData());
        System.out.println("---------------------------------------------------------------");

        if (text.startsWith("STOR "))
        {
            lastUploadFile = text.substring(5).trim();
            totalBytes.set(0);
        }

        return super.onRequest(message);
    }

    // 当收到一整行的上游FTP服务器的响应消息时触发
    @Override
    public Message onResponse(Message message)
    {
        String text = message.getText();

        System.out.println("Response: ");
        System.out.println(text.trim());
        ByteUtils.dump(message.getData());
        System.out.println("---------------------------------------------------------------");

        if (text.startsWith("226 "))
        {
            System.out.println("File: " + lastUploadFile + " upload completed with " + totalBytes + " bytes");
            lastUploadFile = null;
            totalBytes.set(0);
        }

        return super.onResponse(message);
    }

    // 注意，以下两个方法与上面四个方法不在同一个线程内执行，注意对共享变量的并发写控制
    // 当收到下游FTP客户端的透传数据时触发，在上传一个文件时，可能会按1024字节为一个块进行传输，会调用很多次
    @Override
    public void onPassiveRequest(byte[] data, int len)
    {
        totalBytes.addAndGet(len);
        System.err.println("Passive Upload: " + lastUploadFile + " : " + totalBytes);
    }

    // 当收到上游FTP服务器的透传数据时触发，在下载一个文件时，可能会按1024字节为一个块进行传输，会调用很多次
    @Override
    public void onPassiveResponse(byte[] data, int len)
    {
        System.err.println("Passive Download: " + len);
    }
}
