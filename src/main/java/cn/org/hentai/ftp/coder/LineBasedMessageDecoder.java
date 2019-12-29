package cn.org.hentai.ftp.coder;

import cn.org.hentai.ftp.message.ASCIIMessage;
import cn.org.hentai.ftp.message.Message;
import cn.org.hentai.ftp.util.ByteHolder;
import cn.org.hentai.ftp.util.ByteUtils;

/**
 * Created by matrixy on 2019/12/28.
 */
public class LineBasedMessageDecoder
{
    private ByteHolder buffer;

    public LineBasedMessageDecoder()
    {
        this.buffer = new ByteHolder(4096);
    }

    public void write(byte[] data, int len)
    {
        this.buffer.write(data, 0, len);
    }

    public Message decode()
    {
        for (int i = 0, l = buffer.size() - 1; i < l; i++)
        {
            int a = buffer.get(i);
            int b = buffer.get(i + 1);

            if (a == 0x0D && b == 0x0A)
            {
                byte[] data = new byte[i + 2];
                buffer.sliceInto(data, i + 2);
                return new ASCIIMessage(data);
            }
        }
        return null;
    }
}
