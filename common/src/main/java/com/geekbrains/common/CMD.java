package com.geekbrains.common;

/**
 * Created by User on 16.11.2018.
 */
public class CMD extends AbstractObject {
    String command;
    String status;

    public CMD(String command, String status) {
        this.command = command;
        this.status = status;
    }
}
