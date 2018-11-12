package com.geekbrains.common;

import java.nio.file.Path;

public class FileRequest extends AbstractObject {
    private String filename;
    private Path path;

    public String getFilename() {
        return filename;
    }

    public FileRequest(Path path, String filename) {
        this.path = path;
        this.filename = filename;
    }
}
