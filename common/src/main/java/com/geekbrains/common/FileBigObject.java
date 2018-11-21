package com.geekbrains.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBigObject extends AbstractObject {
    private String filename;
    private byte[] data;
    private int partCount;
    private int curPart;
    private String hash;
    private long sizeFile;

    public String getFilename() {
        return filename;
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
        this.filename = name;
        this.data = data;
        this.curPart = curPart;
        this.partCount = partCount;
        this.hash = hash;
        this.sizeFile = size;

    }
}
