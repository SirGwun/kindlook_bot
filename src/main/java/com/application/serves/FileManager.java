package com.application.serves;

import com.application.Main;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileManager {
    public static Path getDataFilePath(String fileName) {
        Path base = Main.inAmvera() ? Path.of("/data") : Path.of("data");
        return base.resolve(fileName);
    }

    public static List<Path> getImagePaths(List<String> imagePathList) throws FileNotFoundException {
        if (imagePathList == null || imagePathList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Path> imageFiles = new ArrayList<>();
        for (String fileName : imagePathList) {
            imageFiles.add(getImagePath(fileName));
        }
        return imageFiles;
    }

    public static Path getImagePath(String fileName) throws FileNotFoundException {
        if (fileName.isEmpty()) {
            throw new FileNotFoundException("Имя файла пустое");
        }
        Path base = Path.of(Main.inAmvera() ? "/data" : "data", "images");
        return base.resolve("fileName");
    }
}
