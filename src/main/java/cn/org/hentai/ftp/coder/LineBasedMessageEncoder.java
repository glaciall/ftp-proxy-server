package cn.org.hentai.ftp.coder;

import cn.org.hentai.ftp.message.Message;

/**
 * Created by matrixy on 2019/12/28.
 */
public class LineBasedMessageEncoder
{
    public byte[] encode(Message message)
    {
        return message.getData();
    }
}
