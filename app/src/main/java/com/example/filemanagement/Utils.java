package com.example.filemanagement;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static List<File> getListFiles(File file) {
        File[] list = file.listFiles();
        if (list != null) {
            return new ArrayList<>(Arrays.asList(list));
        }
        return null;
    }

    public static Model fromFileToFileModel(File file) {
        return new Model(file.getName(), file.getAbsolutePath(), getFileType(file));
    }

    public static Type getFileType(File file) {
        if (file.isDirectory()) {
            return Type.DIRECTORY;
        }
        String extension;
        int index = file.getName().lastIndexOf(".");
        if (index > 0) {
            extension = file.getName().substring(index + 1);
        } else {
            return null;
        }

        if (extension.equals("jpg")
                || extension.equals("jpeg")
                || extension.equals("webp")
                || extension.equals("png")
                || extension.equals("bmp")
                || extension.equals("tiff")
        ) {
            return Type.IMAGE;
        }

        if (extension.equals("txt")) {
            return Type.TEXT;
        }

        return null;

    }
}
