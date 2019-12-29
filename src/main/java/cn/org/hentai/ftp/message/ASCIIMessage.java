package cn.org.hentai.ftp.message;

/**
 * Created by matrixy on 2019/12/28.
 */
public class ASCIIMessage extends Message
{
    private String text;
    public ASCIIMessage(byte[] data)
    {
        super(data);
        this.text = new String(data).trim();
    }

    public ASCIIMessage(String text)
    {
        super(text.getBytes());
        if (text.endsWith("\r\n") == false) throw new RuntimeException("no LF/CR for text message ending");
    }

    public String getText()
    {
        return text;
    }
}
