package com.geekbrains.server;

import com.geekbrains.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class CMDHandler extends ChannelInboundHandlerAdapter {

    private String userName;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object income) throws Exception {
        userName = ctx.pipeline().get(MainHandler.class).getUserName();

        try {
            if (income == null) {
                return;
            }

            if (income instanceof CMD){
                CMD cmd = (CMD) income;
                if (cmd.getCommand().equals("/delete")){
                    Files.delete(Paths.get("server_storage/" + userName + "/" + cmd.getFilename()));
                }
            }


        } finally {
            ReferenceCountUtil.release(income);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("CMD" + cause);
        ctx.close();
    }

}
