package com.geekbrains.server;

import com.geekbrains.common.BigDataInfo;
import com.geekbrains.common.CMD;
import com.geekbrains.common.FileBigObject;
import com.geekbrains.common.FileObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BigDataHandler extends ChannelInboundHandlerAdapter {
    private String userName;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object income) throws Exception {
        userName = ctx.pipeline().get(MainHandler.class).getUserName();

        try {
            if (income == null) {
                return;
            }

            if (income instanceof FileBigObject){
                FileBigObject fbo = (FileBigObject) income;

                /*if (new File("server_storage/" + userName + "/" + fbo.getFilename()).exists()){
                    ctx.writeAndFlush(new BigDataInfo(0, 0, "already exists"));
                    return;
                }*/

                byte[] dataPart = fbo.getData();

                if (fbo.getCurPart() == 1){
                    System.out.println("Server get " + fbo.getCurPart());
                    Files.write(Paths.get("server_storage/" + userName + "/" + fbo.getFilename()), fbo.getData(), StandardOpenOption.CREATE);
                    System.out.println("Create file");
                } else {
                    Files.write(Paths.get("server_storage/" + userName + "/" + fbo.getFilename()), fbo.getData(), StandardOpenOption.APPEND);
                    System.out.println("Append file");
                }

                if (fbo.getCurPart() == fbo.getPartCount()){
                    ctx.writeAndFlush(new BigDataInfo(fbo.getCurPart(), fbo.getPartCount(), "getIt"));
                    System.out.println("Отправил запрос на завершение");
                } else {
                    ctx.writeAndFlush(new BigDataInfo(fbo.getCurPart(), fbo.getPartCount(), "next"));
                    System.out.println("Отправил запрос на след часть");
                }

            }
            ctx.fireChannelRead(income);

        } finally {
            ReferenceCountUtil.release(income);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
