package com.dashwood.photodownloader.listener;

public interface CallBackDownloader {
    void onSuccess(byte[] response);

    void onError(Exception e);
}
