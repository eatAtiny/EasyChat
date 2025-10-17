package com.easychat.chat.netty.handler;

import cn.hutool.core.bean.BeanUtil;
import com.easychat.chat.netty.ChannelContextUtils;
import com.easychat.common.entity.dto.TokenUserInfoDTO;
import com.easychat.common.utils.RedisComponet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description ws 业务处理
 * @Author 程序员老罗
 * @Date 2023/12/17 10:10
 */

/**
 * 设置通道共享
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private RedisComponet redisComponet;

    /**
     * 当通道就绪后会调用此方法，通常我们会在这里做一些初始化操作
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Channel channel = ctx.channel();
        log.info("有新的连接加入。。。");
    }

    /**
     * 当通道不再活跃时（连接关闭）会调用此方法，我们可以在这里做一些清理工作
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有连接已经断开。。。");
        channelContextUtils.removeContext(ctx.channel());
    }

    /**
     * 读就绪事件 当有消息可读时会调用此方法，我们可以在这里读取消息并处理。
     *
     * @param ctx
     * @param textWebSocketFrame
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        //接收心跳
        Channel channel = ctx.channel();
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
//        log.info("接收到用户{}的心跳",userId);
        redisComponet.saveUserHeartBeat(userId);
    }


    //用于处理用户自定义的事件  当有用户事件触发时会调用此方法，例如连接超时，异常等。
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // ws握手完成事件,  握手完成后，我们可以从请求中获取token，验证token是否有效，有效则加入到channelContextUtils中
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String url = complete.requestUri();
            String token = getToken(url);
            if (token == null) {
                ctx.channel().close();
                return;
            }
            TokenUserInfoDTO tokenUserInfoDto = redisComponet.getTokenUserInfoDTO(token);
            if (null == tokenUserInfoDto) {
                ctx.channel().close();
                return;
            }
            /**
             * 用户加入
             */
            channelContextUtils.addContext(tokenUserInfoDto.getUserId(), ctx.channel());

        }
    }

    private String getToken(String url) {
        if (BeanUtil.isEmpty(url) || url.indexOf("?") == -1) {
            return null;
        }
        String[] queryParams = url.split("\\?");
        if (queryParams.length < 2) {
            return url;
        }
        String[] params = queryParams[1].split("=");
        if (params.length != 2) {
            return url;
        }
        return params[1];
    }
}