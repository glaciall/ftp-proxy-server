package cn.org.hentai.ftp.app;

import cn.org.hentai.ftp.interceptor.PassiveProxyInterceptor;
import cn.org.hentai.ftp.interceptor.TestSessionInterceptor;
import cn.org.hentai.ftp.proxy.*;
import cn.org.hentai.ftp.util.Configs;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Created by matrixy on 2019/12/24.
 */
public class FtpServerApp
{
    static Logger logger = LoggerFactory.getLogger(FtpServerApp.class);

    public static void main(String[] args) throws Exception
    {
        Configs.init("/app.properties");
        ConnectionManager.init();
        PassivePortManager.init();

        final FTPServer ftpServer = new FTPServer();

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // 设置默认的会话消息拦截器
        FTPSession.setDefaultSessionInterceptor(TestSessionInterceptor.class);

        Signal.handle(new Signal("TERM"), new SignalHandler()
        {
            @Override
            public void handle(Signal signal)
            {
                ftpServer.shutdown();
            }
        });

        ftpServer.start();
    }

    static class FTPServer
    {
        private static ServerBootstrap serverBootstrap;

        private static EventLoopGroup bossGroup;
        private static EventLoopGroup workerGroup;

        private static void start() throws Exception
        {
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, Configs.getInt("server.backlog", 1024));
            bossGroup = new NioEventLoopGroup(Configs.getInt("server.worker-count", Runtime.getRuntime().availableProcessors()));
            workerGroup = new NioEventLoopGroup();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel channel) throws Exception {
                            ChannelPipeline p = channel.pipeline();
                            p.addLast(new FTPMessageDecoder());
                            p.addLast(new FTPMessageEncoder());
                            p.addLast(new FTPHandler());
                        }
                    });

            int port = Configs.getInt("ftp.proxy.port", 21);
            Channel ch = serverBootstrap.bind(port).sync().channel();
            logger.info("FTP Server started at: {}", port);
            ch.closeFuture();
        }

        private static void shutdown()
        {
            try
            {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
