package com.easychat.chat.netty;

import com.easychat.chat.netty.handler.HeartBeatHandler;
import com.easychat.chat.netty.handler.WebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NettyServerBoot {


    @Resource
    private WebSocketHandler webSocketHandler;

    /**
     * boss线程组，用于处理连接
     */
    private final EventLoopGroup bossGroup;
    /**
     * work线程组，用于处理消息
     */
    private final EventLoopGroup workerGroup;


    private final NettyProperties nettyProperties;

    public NettyServerBoot(NettyProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
        this.bossGroup = new NioEventLoopGroup(nettyProperties.getBoss());
        this.workerGroup = new NioEventLoopGroup(nettyProperties.getWorker());
    }

    /**
     * 启动netty
     *
     * @throws InterruptedException
     */
    @PostConstruct
    public void start() throws InterruptedException {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    // 绑定线程组 boosGroup主线程负责连接 workerGroup负责处理消息
                    .group(bossGroup, workerGroup)
                    // 服务器通道类型
                    .channel(NioServerSocketChannel.class)
                    // 服务器通道处理
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    // 子通道处理
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            // 设置http协议解码器，编码器
                            pipeline.addLast(new HttpServerCodec());
                            // 聚合http请求，将多个http请求合并为一个FullHttpRequest
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            /* 空闲检测，60秒未收到数据，触发心跳检测
                               readIdleTimeSeconds: 60秒未收到数据，触发心跳检测
                               writeIdleTimeSeconds: 0不触发心跳检测
                               allIdleTimeSeconds: 0不触发心跳检测
                               */
                            pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                            // 自定义心跳检测处理器
                            pipeline.addLast(new HeartBeatHandler());
                            //将http协议升级为ws协议，对websocket支持
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 64 * 1024, true, true, 10000L));
                            // 共享ws协议消息处理器
                            pipeline.addLast(webSocketHandler);
                        }
                    });
            // 绑定端口启动
            serverBootstrap.bind(nettyProperties.getPort()).sync();
            // 备用端口
            serverBootstrap.bind(nettyProperties.getPortSalve()).sync();
            log.info("启动Netty: {},{}", nettyProperties.getPort(), nettyProperties.getPortSalve());

        } catch (Exception e) {
            log.error("启动Netty失败", e);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            throw e;

        } finally {
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
        }

    }

    /**
     * 关闭netty
     */
    @PreDestroy
    public void close() {
        log.info("关闭Netty");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}