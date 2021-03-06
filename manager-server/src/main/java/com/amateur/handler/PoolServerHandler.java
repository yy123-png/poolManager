package com.amateur.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.amateur.detector.PoolInfoContainer;
import com.amateur.info.ClientInfo;
import com.amateur.info.PoolInfo;
import com.amateur.info.PoolParam;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yeyu
 * @date 2021/12/9 16:42
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class PoolServerHandler extends SimpleChannelInboundHandler<String> {

    @Resource
    private PoolInfoContainer poolInfoContainer;

    /**
     * 存储客户端对应的channel组
     */
    private final static ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("receive from client:{}", msg);
        ClientInfo clientInfo = JSON.parseObject(msg, ClientInfo.class);
        List<PoolInfo> poolInfos = clientInfo.getPoolList();
        String remoteAddress = ctx.channel().remoteAddress().toString().substring(1);
        poolInfoContainer.getMap().put(remoteAddress, poolInfos);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        CHANNEL_GROUP.add(ctx.channel());
        log.info("client [{}] is active", ctx.channel().remoteAddress().toString().substring(1));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddress = ctx.channel().remoteAddress().toString().substring(1);
        poolInfoContainer.clearByAddress(remoteAddress);
        log.info("client [{}] is inactive", remoteAddress);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String remoteAddress = ctx.channel().remoteAddress().toString().substring(1);
        poolInfoContainer.clearByAddress(remoteAddress);
        ctx.close();
        log.info("client [{}] is inactive because of exception", remoteAddress, cause);
    }

    public Boolean modifyPoolInfo(PoolParam param) {
        List<PoolInfo> poolInfos = poolInfoContainer.getMap().get(param.getRemoteAddress());
        if (CollectionUtils.isEmpty(poolInfos)) {
            return Boolean.FALSE;
        }

        PoolInfo poolInfo = poolInfos.stream()
                .filter(it -> it.getPoolBeanName().equals(param.getBeanName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("not found beanName：" + param.getBeanName()));

        poolInfo.setCorePoolSize(param.getCorePoolSize());
        poolInfo.setMaximumPoolSize(param.getMaxPoolSize());
        poolInfo.setKeepAliveTime(param.getKeepAliveTime());

        Channel channel = CHANNEL_GROUP.stream()
                .filter(it -> it.remoteAddress().toString().substring(1).equals(param.getRemoteAddress()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("not found channel with remoteAddress:" + param.getRemoteAddress()));

        channel.writeAndFlush(JSON.toJSONString(param));

        return Boolean.TRUE;
    }
}
