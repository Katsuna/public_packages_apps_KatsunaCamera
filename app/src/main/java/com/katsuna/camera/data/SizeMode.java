package com.katsuna.camera.data;

public enum SizeMode {
    SMALL, LARGE;

    public static SizeMode toEnum(String sizeModeStr) {
        try {
            return valueOf(sizeModeStr);
        } catch (Exception ex) {
            // For error cases
            return defaultMode();
        }
    }

    public static SizeMode defaultMode() {
        return SizeMode.LARGE;
    }
}
