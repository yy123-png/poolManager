package com.amateur.handler;

import com.alibaba.fastjson.JSON;
import com.amateur.config.Properties;
import com.amateur.context.PoolContext;
import com.amateur.listener.RetryListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author yeyu
 * @date 2021/12/9 16:33
 */
@Component
@ChannelHandler.Sharable
@Slf4j
public class PoolClientHandler extends SimpleChannelInboundHandler<String> {

    @Resource
    private Properties properties;

    @Resource
    private PoolContext poolContext;

    @Resource
    private RetryListener retryListener;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.writeAndFlush(JSON.toJSONString(poolContext.getClientInfo()));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("receive from server:{}", msg);
        //TODO: 更新线程池信息
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(JSON.toJSONString(poolContext.getClientInfo()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("disconnect from server,try to reconnect server");
        ctx.channel().connect(properties.getDefaultConnectAddress()).addListener(retryListener);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
