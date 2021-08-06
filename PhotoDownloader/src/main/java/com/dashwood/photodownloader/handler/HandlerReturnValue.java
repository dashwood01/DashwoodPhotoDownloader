package com.dashwood.photodownloader.handler;

public class HandlerReturnValue {
    public static String getFileName(String filepath) {
        String[] strings = filepath.split("/");
        return strings[strings.length - 1];
    }
}
