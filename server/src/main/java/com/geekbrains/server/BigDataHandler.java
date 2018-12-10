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
    private static final int MAX_OBJ_SIZE = 1024 * 1024 * 25;
    private InputStream in;
    private byte[] arrPartData = new byte[MAX_OBJ_SIZE];

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
                if (bdi.getStatus().equals("NextPart")){
                    String filePath = "server_storage/" + userName + "/" + bdi.getFileName();
                    String fileName = bdi.getFileName();
                    Long fileSize = Paths.get(filePath).toFile().length();
                    if (bdi.getNumPart() == 1) {
                        in.read(arrPartData);
                        ctx.writeAndFlush(new FileBigObject(fileName, arrPartData, bdi.getNumPart(), bdi.getPartsCount()));
                    }
                    if (bdi.getNumPart() > 1 && bdi.getNumPart() < bdi.getPartsCount()){
                        in.read(arrPartData);
                        ctx.writeAndFlush(new FileBigObject(fileName, arrPartData, bdi.getNumPart(), bdi.getPartsCount()));
                    }
                    if (bdi.getNumPart() == bdi.getPartsCount()){
                        byte[] arrLastPartData = new byte[(int) (fileSize - MAX_OBJ_SIZE * (bdi.getPartsCount() - 1))];
                        in.read(arrLastPartData);
                        ctx.writeAndFlush(new FileBigObject(fileName, arrLastPartData, bdi.getNumPart(), bdi.getPartsCount()));
                        in.close();
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
    public void sendDataToClient(FileRequest fr, ChannelHandlerContext ctx) throws IOException {
        if (Files.exists(Paths.get("server_storage/" + userName + "/" + fr.getFilename()))) {
            if (Paths.get("server_storage/" + userName + "/" + fr.getFilename()).toFile().length() > MAX_OBJ_SIZE){
                sendBigDataToClient(fr, ctx);
            } else {
                FileObject fm = new FileObject(Paths.get("server_storage/" + userName + "/" + fr.getFilename()));
                ctx.writeAndFlush(fm);
            }
        }
    }


    public void sendBigDataToClient(FileRequest fr, ChannelHandlerContext ctx) throws FileNotFoundException {
        String filePath = "server_storage/" + userName + "/" + fr.getFilename();
        String fileName = fr.getFilename();
        Long fileSize = Paths.get(filePath).toFile().length();

        int partCount = (int) Math.ceil(fileSize / Double.parseDouble(String.valueOf(MAX_OBJ_SIZE)));
        ctx.writeAndFlush(new BigDataInfo(0, partCount, "ReadyToSend", fileName));
        in = new BufferedInputStream(new FileInputStream(filePath));

        /*try {
            File file = new File(filePath);
            String hash = toHash(fileSize, fileName);
            byte[] arrPartData = new byte[MAX_OBJ_SIZE];
            byte[] arrLastPartData = new byte[(int) (fileSize - MAX_OBJ_SIZE * (partCount - 1))];
            InputStream in = new BufferedInputStream(new FileInputStream(file)); //Открываем поток
            //int ipart = 0;



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
        }*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("BDH " + cause);
        ctx.close();
    }

    public static String toHash(long data, String name) {
        return DigestUtils.md5Hex(String.valueOf(data) + name);
    }

}
