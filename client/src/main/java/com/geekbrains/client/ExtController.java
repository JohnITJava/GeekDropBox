package com.geekbrains.client;

import com.geekbrains.common.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtController {
    private static final int MAX_OBJ_SIZE = 1024 * 1024 * 4; //4 Mb


    public static void sendBigData(Path path) {

        Thread bigDataSender = new Thread(() -> {
            String filePath = path.toString();
            String fileName = Paths.get(filePath).getFileName().toString();
            Long fileSize = path.toFile().length();

            int partCount = (int) Math.ceil(fileSize / Double.parseDouble(String.valueOf(MAX_OBJ_SIZE)));

            try {
                File file = new File(filePath);
                String hash = toHash(fileSize, fileName);
                byte[] arrPartData = new byte[MAX_OBJ_SIZE];
                byte[] arrLastPartData = new byte[(int) (fileSize - MAX_OBJ_SIZE * (partCount - 1))];
                InputStream in = new BufferedInputStream(new FileInputStream(file)); //Открываем поток
                int ipart = 0;

                Network.sendObject(new BigDataInfo(0, partCount, "ReadyToSend", fileName));

                ipart += 1;
                for (int i = 0; i < partCount-1; i++) {
                    in.read(arrPartData);
                    Network.sendObject(new FileBigObject(fileName, arrPartData, ipart, partCount, hash, fileSize));
                    ipart += 1;
                }

                in.read(arrLastPartData);
                Network.sendObject(new FileBigObject(fileName, arrLastPartData, ipart, partCount, hash, fileSize));
                in.close();

                Network.sendObject(new BigDataInfo("Over", fileName));

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        bigDataSender.setDaemon(true);
        bigDataSender.start();
    }

    public synchronized static boolean bigDataHandler(BigDataInfo info) {
        if (info.getStatus().equals("next")) {
            return true;
        }
        if (info.getStatus().equals("getIt")) {
            return true;
        }
        if (info.getStatus().equals("already exists")) {
            return false;
        }
        return false;
    }

    public static void receiveBigData(){

    }

    public static String toHash(long data, String name) {
        return DigestUtils.md5Hex(String.valueOf(data) + name);
    }

}
