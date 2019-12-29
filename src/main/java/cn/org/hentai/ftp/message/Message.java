package cn.org.hentai.ftp.message;

import java.io.Serializable;

/**
 * Created by matrixy on 2019/12/28.
 */
public abstract class Message implements Serializable
{
    private byte[] data;
    public Message(byte[] data)
    {
        this.data = data;
    }

    public abstract String getText();

    public byte[] getData()
    {
        return this.data;
    }
}
