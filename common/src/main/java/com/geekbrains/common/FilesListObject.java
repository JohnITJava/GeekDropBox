package com.geekbrains.common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class FilesListObject extends AbstractObject{
    private List<String> fileNamesList;

    public List<String> getFileNamesList() {
        return fileNamesList;
    }

    public FilesListObject(List<String> fileNamesList) {
        this.fileNamesList = fileNamesList;
    }
}
