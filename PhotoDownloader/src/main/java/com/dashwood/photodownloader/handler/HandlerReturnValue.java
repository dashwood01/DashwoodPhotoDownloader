package com.dashwood.photodownloader.handler;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

    public static String getNowDate() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ROOT);
        return format.format(new Date());
    }

    public static boolean checkTimeForClear(String stringDate, int time) {
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ROOT);
            Calendar date = Calendar.getInstance();
            date.setTime(Objects.requireNonNull(format.parse(stringDate)));
            long timeInSecs = date.getTimeInMillis();
            Date afterAddingTimeMins = new Date(timeInSecs + ((long) time * 60 * 1000));
            Date nowDate = format.parse(getNowDate());
            if (afterAddingTimeMins.after(nowDate)) {
                long diff = (nowDate != null ? nowDate.getTime() : 0) - afterAddingTimeMins.getTime();
                return TimeUnit.MILLISECONDS.toMinutes(diff) >= time;
            } else {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeDirectory(File directory) {
        if (directory == null)
            return false;
        if (!directory.exists())
            return true;
        if (!directory.isDirectory())
            return false;

        String[] list = directory.list();
        if (list != null) {
            for (String s : list) {
                File entry = new File(directory, s);
                if (entry.isDirectory()) {
                    if (!removeDirectory(entry))
                        return false;
                } else {
                    if (!entry.delete())
                        return false;
                }
            }
        }
        return directory.delete();
    }
}
