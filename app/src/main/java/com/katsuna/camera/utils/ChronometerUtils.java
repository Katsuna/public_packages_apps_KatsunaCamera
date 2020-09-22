/**
* Copyright (C) 2020 Manos Saratsis
*
* This file is part of Katsuna.
*
* Katsuna is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Katsuna is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Katsuna.  If not, see <https://www.gnu.org/licenses/>.
*/
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
