package com.geekbrains.common;

import java.nio.file.Path;

public class FileRequest extends AbstractObject {
    private String filename;

    public String getFilename() {
        return filename;
    }

    public FileRequest(String filename) {
        this.filename = filename;
    }
}
