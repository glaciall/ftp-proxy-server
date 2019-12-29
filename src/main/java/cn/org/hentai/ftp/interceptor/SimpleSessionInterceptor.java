package cn.org.hentai.ftp.interceptor;

import cn.org.hentai.ftp.coder.LineBasedMessageDecoder;
import cn.org.hentai.ftp.message.Message;
import cn.org.hentai.ftp.coder.LineBasedMessageEncoder;
import cn.org.hentai.ftp.util.Configs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by matrixy on 2019/12/27.
 * FTP消息拦截器，用于对上下行消息的修改处理，当然也可以不修改
 * 注意：onRequest()、onResponse()方法与onPassiveRequest()、onPassiveResponse()方法不处于同一线程
 */
public abstract class SimpleSessionInterceptor extends AbstractSessionInterceptor
{
    static Logger logger = LoggerFactory.getLogger(SimpleSessionInterceptor.class);

    // 收到客户端的请求消息时
    public abstract Message onRequest(Message message);

    // 收到服务器端的回应消息时
    public abstract Message onResponse(Message message);

    // 收到Passive模式下的客户端请求数据
    public abstract void onPassiveRequest(byte[] data, int len);

    // 收到Passive模式下的服务器端响应数据
    public abstract void onPassiveResponse(byte[] data, int len);

    LineBasedMessageEncoder messageEncoder = new LineBasedMessageEncoder();
    LineBasedMessageDecoder clientMessageDecoder = new LineBasedMessageDecoder();
    LineBasedMessageDecoder upstreamMessageDecoder = new LineBasedMessageDecoder();

    @Override
    public byte[] onClientData(byte[] data, int len)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
        clientMessageDecoder.write(data, len);
        while (true)
        {
            Message rawMsg = clientMessageDecoder.decode();
            if (rawMsg == null) break;
            Message msg = onRequest(rawMsg);
            try
            {
                baos.write(messageEncoder.encode(msg));
            }
            catch(Exception ex) { throw new RuntimeException(ex); }
        }
        return baos.toByteArray();
    }

    @Override
    public byte[] onUpstreamData(byte[] data, int len)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        upstreamMessageDecoder.write(data, len);
        while (true)
        {
            Message rawMsg = upstreamMessageDecoder.decode();
            if (rawMsg == null) break;
            Message msg = onResponse(rawMsg);
            try
            {
                baos.write(messageEncoder.encode(msg));
            }
            catch(Exception ex) { throw new RuntimeException(ex); }
        }
        return baos.toByteArray();
    }
}
