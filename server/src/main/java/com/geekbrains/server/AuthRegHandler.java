package com.geekbrains.server;

import com.geekbrains.common.AuthObject;
import com.geekbrains.common.AuthRequest;
import com.geekbrains.common.RegObject;
import com.geekbrains.common.RegRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AuthRegHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object income) throws Exception {
        try {
            if (income == null) {
                return;
            }
            if (income instanceof AuthRequest) {
                AuthRequest arr = (AuthRequest) income;
                if (SQLHandler.tryToLogIn(((AuthRequest) income).getLogin(), ((AuthRequest) income).getPassword())){
                    AuthObject outcome = new AuthObject(((AuthRequest) income).getLogin(), true);
                    ctx.writeAndFlush(outcome);
                } else {
                    AuthObject outcome = new AuthObject(((AuthRequest) income).getLogin(),false);
                    ctx.writeAndFlush(outcome);
                }
            }
            if (income instanceof RegRequest){
                RegRequest arr = (RegRequest) income;
                if (SQLHandler.tryToRegister(((RegRequest) income).getLogin(), ((RegRequest) income).getPassword())){
                    RegObject outcome = new RegObject(true);
                    String path = "server_storage/" + ((RegRequest) income).getLogin();
                    Files.createDirectory(Paths.get(path));
                    ctx.writeAndFlush(outcome);
                } else {
                    RegObject outcome = new RegObject(false);
                    ctx.writeAndFlush(outcome);
                }
            }
        }
        finally
    {
        ReferenceCountUtil.release(income);
    }

}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
