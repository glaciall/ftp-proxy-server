package cn.org.hentai.ftp.interceptor;

import cn.org.hentai.ftp.message.ASCIIMessage;
import cn.org.hentai.ftp.message.Message;
import cn.org.hentai.ftp.proxy.PassiveDataTransfer;
import cn.org.hentai.ftp.util.ByteUtils;
import cn.org.hentai.ftp.util.FTPUtils;

/**
 * Created by matrixy on 2019/12/28.
 */
public abstract class PassiveProxyInterceptor extends SimpleSessionInterceptor
{
    PassiveDataTransfer passiveDataTransfer = null;

    @Override
    public Message onRequest(Message message)
    {
        return message;
    }

    @Override
    public Message onResponse(Message message)
    {
        String text = message.getText();
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
}
