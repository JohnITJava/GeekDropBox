package com.geekbrains.server;

import com.geekbrains.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class BigDataHandler extends ChannelInboundHandlerAdapter {
    private String userName;
    private static final int MAX_OBJ_SIZE = 1024 * 1024 * 4;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object income) throws Exception {
        userName = ctx.pipeline().get(MainHandler.class).getUserName();

        try {
            if (income == null) {
                return;
            }

            if (income instanceof BigDataInfo) {
                BigDataInfo bdi = (BigDataInfo) income;
                if (bdi.getStatus().equals("ReadyToSend")) {
                    byte[] firstByte = new byte[0];
                    if (!Files.exists(Paths.get("server_storage/" + userName + "/" + bdi.getFileName()))) {
                        Files.write(Paths.get("server_storage/" + userName + "/" + bdi.getFileName()), firstByte, StandardOpenOption.CREATE);
                        System.out.println("Create file");
                    } else {
                        return;
                    }
                }
            }

            if (income instanceof FileBigObject) {
                FileBigObject fbo = (FileBigObject) income;
                Files.write(Paths.get("server_storage/" + userName + "/" + fbo.getFileName()), fbo.getData(), StandardOpenOption.APPEND);
                System.out.println("Append file : " + fbo.getFileName());

                if (fbo.getCurPart() == fbo.getPartCount()){
                    ctx.writeAndFlush(new BigDataInfo("getIt"));
                    System.out.println("Get file fully");
                }
            }

            ctx.fireChannelRead(income);

        } finally {
            ReferenceCountUtil.release(income);
        }
    }

    public void sendBigDataToClient(FileRequest fr, ChannelHandlerContext ctx){
        String filePath = "server_storage/" + userName + "/" + fr.getFilename();
        String fileName = fr.getFilename();
        Long fileSize = Paths.get(filePath).toFile().length();

        int partCount = (int) Math.ceil(fileSize / Double.parseDouble(String.valueOf(MAX_OBJ_SIZE)));

        try {
            File file = new File(filePath);
            String hash = toHash(fileSize, fileName);
            byte[] arrPartData = new byte[MAX_OBJ_SIZE];
            byte[] arrLastPartData = new byte[(int) (fileSize - MAX_OBJ_SIZE * (partCount - 1))];
            InputStream in = new BufferedInputStream(new FileInputStream(file)); //Открываем поток
            int ipart = 0;

            ctx.writeAndFlush(new BigDataInfo("ReadyToSend", fileName));

            ipart += 1;
            for (int i = 0; i < partCount-1; i++) {
                in.read(arrPartData);
                ctx.writeAndFlush(new FileBigObject(fileName, arrPartData, ipart, partCount, hash, fileSize));
                ipart += 1;
            }

            in.read(arrLastPartData);
            ctx.writeAndFlush(new FileBigObject(fileName, arrLastPartData, ipart, partCount, hash, fileSize));
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("BDH" + cause);
        ctx.close();
    }

    public static String toHash(long data, String name) {
        return DigestUtils.md5Hex(String.valueOf(data) + name);
    }

}
