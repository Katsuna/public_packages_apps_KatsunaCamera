package com.katsuna.camera.api;

import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.katsuna.camera.R;
import com.katsuna.camera.data.FlashMode;

import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_ON;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH;
import static android.hardware.camera2.CameraMetadata.FLASH_MODE_OFF;
import static android.hardware.camera2.CameraMetadata.FLASH_MODE_SINGLE;
import static android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE;
import static android.hardware.camera2.CaptureRequest.FLASH_MODE;

public class FlashUtil {

    public static void adjustRequestBuilder(CaptureRequest.Builder builder, FlashMode flashMode) {
        switch (flashMode) {
            case ON:
                builder.set(CONTROL_AE_MODE, CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                builder.set(FLASH_MODE, FLASH_MODE_SINGLE);
                break;
            case OFF:
                builder.set(CONTROL_AE_MODE, CONTROL_AE_MODE_ON);
                builder.set(FLASH_MODE, FLASH_MODE_OFF);
                break;
            case AUTO:
                builder.set(CONTROL_AE_MODE, CONTROL_AE_MODE_ON_AUTO_FLASH);
                break;
        }
    }

    public static void adjustRequestBuilder4Video(CaptureRequest.Builder builder, FlashMode flashMode) {
        switch (flashMode) {
            case ON:
                builder.set(CONTROL_AE_MODE, CONTROL_AE_MODE_ON);
                builder.set(FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                break;
            case OFF:
                builder.set(CONTROL_AE_MODE, CONTROL_AE_MODE_ON);
                builder.set(FLASH_MODE, FLASH_MODE_OFF);
                break;
            case AUTO:
                builder.set(FLASH_MODE, FLASH_MODE_OFF);
                break;
        }
    }

    public static void applyFlashMode(TextView textView, FlashMode flashMode) {
        Integer drawableId = null;
        Integer textId = null;
        if (flashMode == FlashMode.ON) {
            drawableId = R.drawable.ic_flash_on;
            textId = R.string.flash;
        } else if (flashMode == FlashMode.AUTO) {
            drawableId = R.drawable.ic_flash_auto;
            textId = R.string.flash_auto;
        } else if (flashMode == FlashMode.OFF) {
            drawableId = R.drawable.ic_flash_off;
            textId = R.string.flash_off;
        }

        if (textView != null) {
            if (drawableId != null) {
                Drawable dr = ContextCompat.getDrawable(textView.getContext(), drawableId);
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, dr, null, null);
            }

            if (textId != null) {
                textView.setText(textId);
            }
        }
    }
}
