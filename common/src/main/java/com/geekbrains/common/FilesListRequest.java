package com.geekbrains.common;

import java.nio.file.Path;
import java.util.List;

public class FilesListRequest extends AbstractObject {
    private List<String> filenames;
    private Path path;

    public FilesListRequest(Path path) {
        this.path = path;
    }
}
