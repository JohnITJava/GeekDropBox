package com.geekbrains.server;

import com.geekbrains.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                if (Files.exists(Paths.get("server_storage/" + userName + "/" + fr.getFilename()))) {
                    FileObject fm = new FileObject(Paths.get("server_storage/" + userName + "/" + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }

            if (income instanceof FilesListRequest){
                System.out.println(33);
                List<Path> pathList = Files.list(Paths.get("server_storage/" + userName + "/")).collect(Collectors.toList());
                FilesListObject flo = new FilesListObject(pathList);
                System.out.println(34);
                ctx.writeAndFlush(flo);
                System.out.println("Отправлен");
            }

        } finally {
            ReferenceCountUtil.release(income);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
