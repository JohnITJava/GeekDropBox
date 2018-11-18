package com.geekbrains.common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class FilesListObject extends AbstractObject{
    private List<File> serverFiles;

    public List<File> getServerFiles() {
        return serverFiles;
    }

    public FilesListObject(List<File> serverFiles) {
        this.serverFiles = serverFiles;
    }
}
