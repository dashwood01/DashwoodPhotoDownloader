package com.dashwood.photodownloader.handler;

import android.text.TextUtils;

public class HandlerCheckValue {
    public static boolean checkEmptyOrNullValue(String value) {
        if (TextUtils.isEmpty(value)) {
            return true;
        }
        if (value.equals("null")) {
            return true;
        }
        return value.equals("NULL");
    }
}
