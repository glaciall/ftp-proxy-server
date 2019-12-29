package cn.org.hentai.ftp.interceptor;

import cn.org.hentai.ftp.proxy.FTPSession;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by matrixy on 2019/12/27.
 * FTP会话拦截器
 */
public abstract class AbstractSessionInterceptor
{
    // 当收到客户端发来的消息时触发
    public abstract byte[] onClientData(byte[] data, int len);

    // 当收到服务器端的响应时触发
    public abstract byte[] onUpstreamData(byte[] data, int len);
}
