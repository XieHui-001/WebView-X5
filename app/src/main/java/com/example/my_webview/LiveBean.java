package com.example.my_webview;

public class LiveBean {

    public LiveBean(String path, int size, String displayName) {
        this.path = path;
        this.size = size;
        this.displayName = displayName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private String path;
    private int size;
    private String displayName;
}
