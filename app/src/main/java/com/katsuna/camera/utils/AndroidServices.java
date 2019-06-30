package com.katsuna.camera.utils;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;

import timber.log.Timber;

/**
 * Initializable singleton set of android service providers.
 * Some services may be null
 */
@SuppressWarnings("ALL")
public class AndroidServices {
    private static final String TAG = "AndroidServices";
    /** Log all requests; otherwise will only log long requests. */
    private static final boolean LOG_ALL_REQUESTS = false;
    /** Log requests if this threshold is exceeded. */
    private static final int LOG_THRESHOLD_MILLIS = 10;

    private static class Singleton {
        private static final AndroidServices INSTANCE =
                new AndroidServices(AndroidContext.instance().get());
    }

    public static AndroidServices instance() {
        return Singleton.INSTANCE;
    }

    private final Context mContext;
    private AndroidServices(Context context) {
        mContext = context;
    }

    public ActivityManager provideActivityManager() {
        return (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    }

    public AudioManager provideAudioManager() {
        return (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    public AccessibilityManager provideAccessibilityManager() {
        return (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    public CameraManager provideCameraManager() {
        CameraManager cameraManager = null;

        try {
            Object service = ApiHelper.HAS_CAMERA_2_API ? getSystemService(Context.CAMERA_SERVICE)
                    : null;
            cameraManager = (CameraManager) service;
        } catch (IllegalStateException ex) {
            Timber.tag(TAG).e("Could not get the CAMERA_SERVICE");
            cameraManager = null;
        }

        return cameraManager;
    }

    public DevicePolicyManager provideDevicePolicyManager() {
        return (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    public DisplayManager provideDisplayManager() {
        return (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
    }

    public KeyguardManager provideKeyguardManager() {
        return (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
    }

    public LayoutInflater provideLayoutInflater() {
        return (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public LocationManager provideLocationManager() {
        return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public NotificationManager provideNotificationManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public PowerManager providePowerManager() {
        return (PowerManager) getSystemService(Context.POWER_SERVICE);
    }

    public SensorManager provideSensorManager() {
        return (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    public Vibrator provideVibrator() {
        return (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    public WindowManager provideWindowManager() {
        return (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    private Object getSystemService(String service) {
        try {
            long start = System.currentTimeMillis();
            Object result = mContext.getSystemService(service);
            long duration = System.currentTimeMillis() - start;
            if (duration > LOG_THRESHOLD_MILLIS) {
                Timber.tag(TAG).w("Warning: providing system service %s took %d ms", service,
                        duration);
            } else if (LOG_ALL_REQUESTS) {
                Timber.tag(TAG).v("Provided system service %s in %d ms", service, duration);
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}

