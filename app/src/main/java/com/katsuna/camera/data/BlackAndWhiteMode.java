package com.katsuna.camera.data;

public enum BlackAndWhiteMode {
    DISABLED, ENABLED;

    public static BlackAndWhiteMode toEnum(String blackAndWhiteModeStr) {
        try {
            return valueOf(blackAndWhiteModeStr);
        } catch (Exception ex) {
            // For error cases
            return defaultMode();
        }
    }

    public static BlackAndWhiteMode defaultMode() {
        return DISABLED;
    }
}
