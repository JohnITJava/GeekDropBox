package com.geekbrains.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBigObject extends AbstractObject {
    private String fileName;
    private byte[] data;
    private int partCount;
    private int curPart;
    private String hash;
    private long sizeFile;

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public int getPartCount() {
        return partCount;
    }

    public int getCurPart() {
        return curPart;
    }

    public String getHash() {
        return hash;
    }

    public long getSizeFile() {
        return sizeFile;
    }

    public FileBigObject(String name, byte[] data, int curPart, int partCount, String hash, Long size){
        this.fileName = name;
        this.data = data;
        this.curPart = curPart;
        this.partCount = partCount;
        this.hash = hash;
        this.sizeFile = size;
    }

    public FileBigObject(String fileName, byte[] data, int curPart, int partCount) {
        this.fileName = fileName;
        this.data = data;
        this.curPart = curPart;
        this.partCount = partCount;
    }
}
