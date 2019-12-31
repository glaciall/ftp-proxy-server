package cn.org.hentai.ftp.interceptor;

import cn.org.hentai.ftp.message.Message;
import cn.org.hentai.ftp.util.ByteUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by matrixy on 2019/12/31.
 */
public class TestSessionInterceptor extends PassiveProxyInterceptor
{
    String lastUploadFile = null;
    AtomicLong totalBytes = new AtomicLong(0);

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

    @Override
    public void onPassiveRequest(byte[] data, int len)
    {
        totalBytes.addAndGet(len);
        System.err.println("Passive Upload: " + lastUploadFile + " : " + totalBytes);
    }

    @Override
    public void onPassiveResponse(byte[] data, int len)
    {
        System.err.println("Passive Download: " + len);
    }
}
