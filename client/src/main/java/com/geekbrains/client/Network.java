package com.geekbrains.client;

import com.geekbrains.common.AbstractObject;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

public class Network {
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;
    private static final int PORT = 8189;
    private static final int MAX_OBJ_SIZE = 1024 * 1024 * 100; //100 Mb


    public static int getMaxObjSize() {
        return MAX_OBJ_SIZE;
    }

    public static void start() {
        try {
            socket = new Socket("localhost", PORT);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendObject(AbstractObject object) {
        try {
            out.writeObject(object);
            System.out.println("Полетел малыш");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static AbstractObject readObject() throws ClassNotFoundException, IOException {
        System.out.println("Get it for Mommy");
        Object obj = in.readObject();
        return (AbstractObject) obj;
    }
}
