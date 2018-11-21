package com.geekbrains.common;

public class BigDataInfo extends AbstractObject{
    private int numPart;
    private int partsCount;
    private String status;

    public int getNumPart() {
        return numPart;
    }

    public String getStatus() {
        return status;
    }

    public int getPartsCount() {
        return partsCount;
    }

    public BigDataInfo(int numPart, int partsCount, String status) {
        this.numPart = numPart;
        this.partsCount = partsCount;
        this.status = status;
    }
}
