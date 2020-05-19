package com.hawkerry.vendor.utils;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

public class HelperClass {
    public static String getDate(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);

        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", calendar).toString();
        return date;
    }
}
