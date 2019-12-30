package cn.org.hentai.ftp.interceptor;

import cn.org.hentai.ftp.message.Message;
import cn.org.hentai.ftp.util.ByteUtils;

/**
 * Created by matrixy on 2019/12/31.
 */
public class TestSessionInterceptor extends PassiveProxyInterceptor
{
    @Override
    public Message onRequest(Message message)
    {
        String text = message.getText();
        if (text == null || text.length() == 0) throw new RuntimeException("WTF???");

        System.out.println("Response: ");
        System.out.println(text.trim());
        ByteUtils.dump(message.getData());
        System.out.println("---------------------------------------------------------------");
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

        return super.onResponse(message);
    }

    @Override
    public void onPassiveRequest(byte[] data, int len)
    {
        System.err.println("Passive Upload: " + len);
    }

    @Override
    public void onPassiveResponse(byte[] data, int len)
    {
        System.err.println("Passive Download: " + len);
    }
}
