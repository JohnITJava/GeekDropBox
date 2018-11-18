package com.geekbrains.common;

public class File extends AbstractObject {
    private String fileName;
    private long size;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public File(String fileName, long size) {
        this.fileName = fileName;
        this.size = size / 1024;
    }
}
