package com.geekbrains.common;

public class BigDataInfo extends AbstractObject{
    private int numPart;
    private int partsCount;
    private String status;
    private String fileName;

    public int getNumPart() {
        return numPart;
    }

    public String getStatus() {
        return status;
    }

    public int getPartsCount() {
        return partsCount;
    }

    public String getFileName() {
        return fileName;
    }

    public BigDataInfo(String status) {
        this.status = status;
    }

    public BigDataInfo(String status, String fileName) {
        this.status = status;
        this.fileName = fileName;
    }

    public BigDataInfo(int numPart, int partsCount, String status) {
        this.numPart = numPart;
        this.partsCount = partsCount;
        this.status = status;
    }

    public BigDataInfo(int numPart, int partsCount, String status, String fileName) {
        this.numPart = numPart;
        this.partsCount = partsCount;
        this.status = status;
        this.fileName = fileName;
    }
}
