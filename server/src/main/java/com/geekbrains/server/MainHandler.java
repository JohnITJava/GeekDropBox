package com.geekbrains.server;

import com.geekbrains.common.*;
import com.geekbrains.common.File;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import javafx.application.Platform;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private String userName;
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object income) throws Exception {
        try {
            if (income == null) {
                return;
            }

            if (income instanceof UserRequest){
                UserObject uo = new UserObject(userName);
                ctx.writeAndFlush(uo);
            }

            if (income instanceof FileRequest) {
                FileRequest fr = (FileRequest) income;
                ctx.pipeline().get(BigDataHandler.class).sendDataToClient(fr, ctx);
            }

            if (income instanceof FilesListRequest){
                List<File> serverFiles = new ArrayList<>();
                Files.list(Paths.get("server_storage/" + userName + "/"))
                        .forEach(p -> serverFiles.add(new File(p.getFileName().toString(), p.toFile().length())));

                FilesListObject flo = new FilesListObject(serverFiles);
                ctx.writeAndFlush(flo);
            }

            if (income instanceof FileObject){
                FileObject fo = (FileObject) income;
                Files.write(Paths.get("server_storage/" + userName + "/" + fo.getFilename()), fo.getData(), StandardOpenOption.CREATE);
            }

            ctx.fireChannelRead(income);

        } finally {
            ReferenceCountUtil.release(income);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("MainH" + cause);
        ctx.close();
    }


}
