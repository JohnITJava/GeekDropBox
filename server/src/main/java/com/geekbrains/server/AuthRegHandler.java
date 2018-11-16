package com.geekbrains.server;

import com.geekbrains.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.file.Files;
import java.nio.file.Paths;

public class AuthRegHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object income) throws Exception {
        try {

            if (income == null) {
                return;
            }

            if (income instanceof AuthRequest) {
                AuthRequest arr = (AuthRequest) income;
                if (SQLHandler.tryToLogIn(((AuthRequest) income).getLogin(), toHash(((AuthRequest) arr).getPassword()))){
                    AuthObject outcome = new AuthObject(((AuthRequest) income).getLogin(), true);
                    ctx.writeAndFlush(outcome);
                    ctx.pipeline().get(MainHandler.class).setUserName(((AuthRequest) income).getLogin());

                } else {
                    AuthObject outcome = new AuthObject(((AuthRequest) income).getLogin(),false);
                    ctx.writeAndFlush(outcome);
                }
            }

            if (income instanceof RegRequest){
                RegRequest arr = (RegRequest) income;
                if (SQLHandler.tryToRegister(((RegRequest) income).getLogin(), toHash(((RegRequest) arr).getPassword()))){
                    RegObject outcome = new RegObject(true);
                    String path = "server_storage/" + ((RegRequest) income).getLogin();
                    Files.createDirectory(Paths.get(path));
                    ctx.writeAndFlush(outcome);
                } else {
                    RegObject outcome = new RegObject(false);
                    ctx.writeAndFlush(outcome);
                }
            }

            ctx.fireChannelRead(income);

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

    public static String toHash(String pass) {
        return DigestUtils.md5Hex(pass);
    }
}
