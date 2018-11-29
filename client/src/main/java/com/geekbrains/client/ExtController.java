package com.geekbrains.client;

import com.geekbrains.common.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExtController {
    private static final int MAX_OBJ_SIZE = 1024 * 1024 * 4; //4 Mb
    private static boolean isNext = false;
    private static boolean isSendIt = false;

    public static void sendBigData(Path path) {

        Thread bigDataSender = new Thread(() -> {
            System.out.println("Вошли в тред");
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
                System.out.println("Предупредили сервер о передаче");

                while (true) {

                    Thread.sleep(1);

                    if (isSendIt) {
                        break;
                    }
                    if (isNext) {
                        isNext = false;
                        ipart += 1;

                        if (ipart < partCount) {
                            for (int j = 0; j < arrPartData.length; j++) {
                                arrPartData[j] = (byte) in.read();
                            }
                            Network.sendObject(new FileBigObject(fileName, arrPartData, ipart, partCount, hash, fileSize));
                        }

                        if (ipart == partCount) {
                            for (int j = 0; j < arrLastPartData.length; j++) {
                                arrLastPartData[j] = (byte) in.read();
                            }
                            Network.sendObject(new FileBigObject(fileName, arrLastPartData, partCount, partCount, hash, fileSize));
                        }
                    }
                }

                System.out.println("Завершаем поток");
                isNext = false;
                isSendIt = false;

                in.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        bigDataSender.setDaemon(true);
        bigDataSender.start();
    }

    public static boolean bigDataHandler(BigDataInfo info) {
        if (info.getStatus().equals("next")) {
            System.out.println("получили запрос на некст");
            isNext = true;
            return false;
        }
        if (info.getStatus().equals("getIt")) {
            System.out.println("получили подтверждение получения всего файла");
            isSendIt = true;
            return true;
        }
        if (info.getStatus().equals("already exists")) {
            return false;
        }
        return false;
    }

    public static String toHash(long data, String name) {
        return DigestUtils.md5Hex(String.valueOf(data) + name);
    }

}
