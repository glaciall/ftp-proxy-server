package cn.org.hentai.ftp.message;

/**
 * Created by matrixy on 2019/12/28.
 */
public class BinaryMessage extends Message
{
    public BinaryMessage(byte[] data)
    {
        super(data);
    }

    @Override
    public String getText()
    {
        return "[binary message]";
    }
}
