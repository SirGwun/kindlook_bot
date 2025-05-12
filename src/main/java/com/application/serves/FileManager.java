package com.application.serves;

import com.application.Main;

import java.nio.file.Path;

public class FileManager {
    public static Path getFilePatch(String fileName) {
        if (Main.inAmvera()) {
            return Path.of("/data", addImageFolderIfNeeded(fileName));
        }
        return Path.of("data", addImageFolderIfNeeded(fileName));
    }

    private static String addImageFolderIfNeeded(String fileName) {
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return Path.of("images", fileName).toString();
        }
        return fileName;
    }
}
