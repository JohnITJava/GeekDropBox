package com.geekbrains.common;

/**
 * Created by User on 16.11.2018.
 */
public class CMD extends AbstractObject {
    String command;
    String filename;
    String status;

    public String getCommand() {
        return command;
    }

    public String getFilename() {
        return filename;
    }

    public String getStatus() {
        return status;
    }

    public CMD(String command, String filename, String status) {
        this.command = command;
        this.filename = filename;
        this.status = status;
    }
}
