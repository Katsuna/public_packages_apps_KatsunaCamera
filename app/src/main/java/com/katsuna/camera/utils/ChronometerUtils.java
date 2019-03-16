package com.katsuna.camera.utils;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.widget.Chronometer;

public class ChronometerUtils {

    private static final int secInMillis = 1000;
    private static final int minuteInMillis = secInMillis * 60;
    private static final int hourInMillis = minuteInMillis * 60;

    @SuppressLint("DefaultLocale")
    public static void adjustFormat(Chronometer chronometer) {
        chronometer.setOnChronometerTickListener(cArg -> {
            long time = SystemClock.elapsedRealtime() - cArg.getBase();
            int h = (int) (time / hourInMillis);
            int m = (int) (time - h * hourInMillis) / minuteInMillis;
            int s = (int) (time - h * hourInMillis - m * minuteInMillis) / secInMillis;
            cArg.setText(String.format("%02d:%02d:%02d", h, m, s ));
        });
    }

    @SuppressLint("SetTextI18n")
    public static void reset(Chronometer chronometer) {
        chronometer.setText("00:00:00");
    }
}
