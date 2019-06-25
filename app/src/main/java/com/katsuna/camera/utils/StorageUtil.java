package com.katsuna.camera.utils;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

public class StorageUtil {

    private static final File PICTURES_DIRECTORY =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    public static final String KATSUNA_CAMERA = "KatsunaCamera";
    private static final File KATSUNA_CAMERA_DIRECTORY = new File(PICTURES_DIRECTORY, KATSUNA_CAMERA);

    private static final long VIDEO_SIZE_THRESHOLD_MB = 200;
    private static final long PICTURE_SIZE_THRESHOLD_MB = 20;

    public static File getVideoFilePath() throws IOException {
        tryToCreateDirectory(KATSUNA_CAMERA_DIRECTORY);
        return new File(KATSUNA_CAMERA_DIRECTORY, getTimestamp() + ".mp4");
    }

    public static File getPhotoFilePath() throws IOException {
        tryToCreateDirectory(KATSUNA_CAMERA_DIRECTORY);

        File file = new File(KATSUNA_CAMERA_DIRECTORY, getTimestamp() + ".jpg");

        boolean fileCreated = file.createNewFile();
        if (!fileCreated) {
            String fileNotCreated = "Couldn't create file: " + file.getAbsolutePath();
            Timber.e(fileNotCreated);
            throw new IOException(fileNotCreated);
        }

        return file;
    }

    private static void tryToCreateDirectory(File file) throws IOException {
        if (!file.exists()) {
            boolean pathCreated = file.mkdirs();

            if (!pathCreated) {
                String pathNotCreated = "Couldn't create path: " + file.getAbsolutePath();
                Timber.e(pathNotCreated);
                throw new IOException(pathNotCreated);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
    }

    // result in MB
    public static long getAvailableSpace() {
        StatFs stat = new StatFs(PICTURES_DIRECTORY.getPath());
        stat.getFreeBytes();
        long megAvailable = stat.getFreeBytes() / 1048576;
        Timber.d("free space %d", megAvailable);
        return megAvailable;
    }

    public static boolean hasAvailableSpaceToRecordVideo() {
        long freeSpace = getAvailableSpace();
        return (freeSpace > VIDEO_SIZE_THRESHOLD_MB);
    }

    public static boolean hasAvailableSpaceToCapturePicture() {
        long freeSpace = getAvailableSpace();
        return (freeSpace > PICTURE_SIZE_THRESHOLD_MB);
    }

    public static boolean storageReady() {
        boolean storageWritable = false;
        boolean directoryExists = false;

        // check storage state
        StorageState storageState = getExternalStorageState();
        if (storageState == StorageState.WRITEABLE) {
            storageWritable = true;
        } else {
            Timber.e("External storage not writable.");
        }

        // check media container folder and create if needed
        if (KATSUNA_CAMERA_DIRECTORY.exists()) {
            directoryExists = true;
        } else {
            try {
                tryToCreateDirectory(KATSUNA_CAMERA_DIRECTORY);
                directoryExists = true;
            } catch (IOException ex) {
                Timber.e("Couldn't create KATSUNA_CAMERA_DIRECTORY.");
            }
        }

        return storageWritable && directoryExists;
    }

    private enum StorageState {NOT_AVAILABLE, WRITEABLE, READ_ONLY}

    private static StorageState getExternalStorageState() {
        StorageState result = StorageState.NOT_AVAILABLE;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return StorageState.WRITEABLE;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return StorageState.READ_ONLY;
        }
        return result;
    }
}
