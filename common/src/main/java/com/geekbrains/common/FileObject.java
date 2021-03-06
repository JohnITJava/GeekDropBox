package com.geekbrains.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileObject extends AbstractObject {
    private String filename;
    private byte[] data;

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public FileObject(Path path) throws IOException {
        filename = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }
}
