package cn.org.hentai.ftp.util;

/**
 * Created by matrixy on 2019/12/29.
 */
public final class FTPUtils
{
    // 解析响应文本里的PASV端口号
    public static int parsePassivePort(String text)
    {
        int port = 0;
        if (text.indexOf('|') > -1)
        {
            port = Integer.parseInt(text.replaceAll("^\\d+.+?(\\d+)\\|(\\d+)?\\)$", "$1"));
        }
        else
        {
            String[] parts = text.replaceAll("^.+?\\((.+?)\\)\\.?$", "$1").split(",");
            if (parts.length != 6) throw new RuntimeException("inform passive message: " + text);
            port = (Integer.parseInt(parts[4]) << 8) | Integer.parseInt(parts[5]);
        }
        return port;
    }
}
