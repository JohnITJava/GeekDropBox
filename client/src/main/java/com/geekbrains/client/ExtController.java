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
    private static final Object monitor = new Object();

    public static synchronized void sendBigData(Path path) {

        Thread bigDataSender = new Thread(() -> {
            System.out.println("Start thread sender");
            String filePath = path.toString();
            String fileName = Paths.get(filePath).getFileName().toString();
            Long fileSize = path.toFile().length();

            int partCount = (int)Math.ceil(fileSize / Double.parseDouble(String.valueOf(MAX_OBJ_SIZE)));

            try {
                    File file = new File(filePath);
                    String hash = toHash(fileSize, fileName);
                    byte[] arrPartData = new byte[MAX_OBJ_SIZE];
                    byte[] arrLastPartData = new byte[(int)(fileSize - MAX_OBJ_SIZE * (partCount - 1))];
                    InputStream in = new BufferedInputStream(new FileInputStream(file)); //Открываем поток

                synchronized (monitor) {
                    for (int i = 0; i < partCount - 1; i++) {
                        System.out.println("Try to read " + i + " part");

                        for (int j = 0; j < arrPartData.length; j++) {
                            arrPartData[j] = (byte) in.read();
                        }

                        Network.sendObject(new FileBigObject(fileName, arrPartData, i + 1, partCount, hash, fileSize));

                        System.out.println("Send " + i + " part");

                        //Thread.currentThread().suspend();

                        monitor.wait();
                    }

                    synchronized (monitor) {
                        System.out.println("Чтение последнего куска");
                        for (int j = 0; j < arrLastPartData.length; j++) {
                            arrLastPartData[j] = (byte) in.read();
                        }
                        Network.sendObject(new FileBigObject(fileName, arrLastPartData, partCount, partCount, hash, fileSize));

                        monitor.wait();
                    }
                }
                System.out.println("Thread closing");
                in.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        //bigDataListener.start();
        bigDataSender.setDaemon(true);
        bigDataSender.start();
    }

    public static synchronized boolean bigDataHandler(BigDataInfo info) {
        System.out.println("Обрабатывыю команду");
        if (info.getStatus().equals("next")) {
            //bigDataSender.notify();
            synchronized (monitor){
                System.out.println("Бужу поток");
            monitor.notifyAll();}
            return true;
        }
        if (info.getStatus().equals("getIt")) {
            System.out.println("Жду решения");
            synchronized (monitor){
                System.out.println("Бужу поток для завершения");
                monitor.notifyAll();}
                return true;
        }
        if (info.getStatus().equals("already exists")) {
            System.out.println("Уже существует");
            return false;
        }
        return false;
    }

    public static String toHash(long data, String name) {
        return DigestUtils.md5Hex(String.valueOf(data) + name);}

}
