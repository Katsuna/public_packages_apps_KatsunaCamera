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

import android.os.Build;

import java.lang.reflect.Field;

@SuppressWarnings("ALL")
public class ApiHelper {
    // Documented value of CPU_ABI on x86 architectures
    private static final String X86ABI = "x86";

    public static final boolean AT_LEAST_16 = Build.VERSION.SDK_INT >= 16;

    public static final boolean HAS_APP_GALLERY =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;

    public static final boolean HAS_ANNOUNCE_FOR_ACCESSIBILITY =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean HAS_AUTO_FOCUS_MOVE_CALLBACK =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean HAS_MEDIA_ACTION_SOUND =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean HAS_SET_BEAM_PUSH_URIS =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean HAS_SURFACE_TEXTURE_RECORDING =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final boolean HAS_ROBOTO_MEDIUM_FONT =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;

    public static final boolean HAS_CAMERA_HDR_PLUS = isKitKatOrHigher();
    public static final boolean HDR_PLUS_CAN_USE_ARBITRARY_ASPECT_RATIOS = isKitKatMR2OrHigher();
    public static final boolean HAS_CAMERA_HDR =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    public static final boolean HAS_DISPLAY_LISTENER =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    public static final boolean HAS_ORIENTATION_LOCK =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    public static final boolean HAS_ROTATION_ANIMATION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    public static final boolean HAS_HIDEYBARS = isKitKatOrHigher();

    // Don't use renderscript for x86 K, L is OK. See b/18435492
    public static final boolean HAS_RENDERSCRIPT =
            !(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && X86ABI.equals(Build.CPU_ABI));

    public static final boolean IS_NEXUS_4 = "mako".equalsIgnoreCase(Build.DEVICE);
    public static final boolean IS_NEXUS_5 = "LGE".equalsIgnoreCase(Build.MANUFACTURER)
            && "hammerhead".equalsIgnoreCase(Build.DEVICE);
    public static final boolean IS_NEXUS_6 = "motorola".equalsIgnoreCase(Build.MANUFACTURER)
            && "shamu".equalsIgnoreCase(Build.DEVICE);
    public static final boolean IS_NEXUS_9 = "htc".equalsIgnoreCase(Build.MANUFACTURER)
            && ("flounder".equalsIgnoreCase(Build.DEVICE)
            || "flounder_lte".equalsIgnoreCase(Build.DEVICE));

    public static final boolean HAS_CAMERA_2_API = isLOrHigher();

    public static int getIntFieldIfExists(Class<?> klass, String fieldName,
                                          Class<?> obj, int defaultVal) {
        try {
            Field f = klass.getDeclaredField(fieldName);
            return f.getInt(obj);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static boolean isKitKatOrHigher() {
        // TODO: Remove CODENAME check as soon as VERSION_CODES.KITKAT is final.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                || "KeyLimePie".equals(Build.VERSION.CODENAME);
    }

    public static boolean isKitKatMR2OrHigher() {
        return isLOrHigher()
                || (isKitKatOrHigher() &&
                ("4.4.4".equals(Build.VERSION.RELEASE) || "4.4.3".equals(Build.VERSION.RELEASE)));
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isLOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                || "L".equals(Build.VERSION.CODENAME) || "LOLLIPOP".equals(Build.VERSION.CODENAME);
    }

    public static boolean isLMr1OrHigher() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isLorLMr1() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP
                || Build.VERSION.SDK_INT == 22; // Lollipop MR1
    }

    public static boolean isMOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                || "MNC".equals(Build.VERSION.CODENAME);
    }
}
