package com.dashwood.photodownloader.handler;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class HandlerReturnValue {
    public static String getFileName(String filepath) {
        String[] strings = filepath.split("/");
        return strings[strings.length - 1];
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

}
