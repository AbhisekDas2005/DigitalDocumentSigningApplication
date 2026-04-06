package com.digitalsigner.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Static file I/O utility methods.
 */
public class FileUtils {

    private FileUtils() { }

    public static byte[] readBytes(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    public static void writeBytes(File file, byte[] data) throws IOException {
        Files.write(file.toPath(), data);
    }

    public static String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase();
    }

    public static File ensureDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
