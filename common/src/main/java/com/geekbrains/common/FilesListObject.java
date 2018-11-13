package com.geekbrains.common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class FilesListObject extends AbstractObject{
    private List<Path> fileNamesList;

    public List<Path> getFileNamesList() {
        return fileNamesList;
    }

    public FilesListObject(List<Path> fileNamesList) throws IOException {
        this.fileNamesList = fileNamesList;
        System.out.println("Obj creat");
    }
}
