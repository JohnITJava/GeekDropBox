package com.geekbrains.server;

import com.geekbrains.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import javafx.application.Platform;

import java.nio.file.*;
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
                System.out.println("Request on server");
                FileRequest fr = (FileRequest) income;
                if (Files.exists(Paths.get("server_storage/" + userName + "/" + fr.getFilename()))) {
                    FileObject fm = new FileObject(Paths.get("server_storage/" + userName + "/" + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            }

            if (income instanceof FilesListRequest){
                List<String> filesList = Files.list(Paths.get("server_storage/" + userName + "/"))
                        .collect(Collectors.toList())
                        .stream()
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
                FilesListObject flo = new FilesListObject(filesList);
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
