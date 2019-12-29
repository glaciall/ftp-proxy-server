package cn.org.hentai.ftp.interceptor;

import cn.org.hentai.ftp.message.ASCIIMessage;
import cn.org.hentai.ftp.message.Message;
import cn.org.hentai.ftp.proxy.PassiveDataTransfer;
import cn.org.hentai.ftp.util.ByteUtils;
import cn.org.hentai.ftp.util.FTPUtils;

/**
 * Created by matrixy on 2019/12/28.
 */
public class MyCustomSessionInterceptor extends SimpleSessionInterceptor
{
    PassiveDataTransfer passiveDataTransfer = null;

    @Override
    public Message onRequest(Message message)
    {
        String text = message.getText();
        if (text == null || text.length() == 0) throw new RuntimeException("WTF???");

        System.out.println("Response: ");
        System.out.println(text.trim());
        ByteUtils.dump(message.getData());
        System.out.println("---------------------------------------------------------------");
        return message;
    }

    @Override
    public Message onResponse(Message message)
    {
        String text = message.getText();

        System.out.println("Response: ");
        System.out.println(text.trim());
        ByteUtils.dump(message.getData());
        System.out.println("---------------------------------------------------------------");

        System.out.println("Test: " + (text.startsWith("227 ") || text.startsWith("229 ")));

        // 创建PAV通道
        if (text.startsWith("227 ") || text.startsWith("229 "))
        {
            int upstreamPassivePort = FTPUtils.parsePassivePort(text);
            passiveDataTransfer = new PassiveDataTransfer(this, upstreamPassivePort);
            int localPassivePort = passiveDataTransfer.allocatePort();
            passiveDataTransfer.start();

            logger.debug("Passive port allocate: {} -> {}", upstreamPassivePort, localPassivePort);

            return new ASCIIMessage("229 Entering Extended Passive Mode (|||" + localPassivePort + "|)\r\n");
        }
        // 关闭数据连接
        else if (text.startsWith("226 "))
        {
            if (passiveDataTransfer != null) passiveDataTransfer.close();
        }
        // 开始传输
        return message;
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
