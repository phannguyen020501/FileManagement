package com.example.filemanagement;

public class Model {
    private String fileName;
    private String absolutePath;
    private Type type;

    public Model(String fileName, String absolutePath, Type type) {
        this.fileName = fileName;
        this.absolutePath = absolutePath;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDirectoryPath() {
        if (type == Type.DIRECTORY) {
            return absolutePath;
        }
        return absolutePath.replace(fileName, "");
    }

    public String getExtension() {
        if (type == Type.DIRECTORY) {
            return "";
        }
        return fileName.split("\\.")[1];
    }
}
