package com.katsuna.camera.utils;

import android.content.Context;
import android.media.Image;
import android.media.MediaScannerConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import timber.log.Timber;

public class ImageSaver implements Runnable {

    /**
     * The JPEG image
     */
    private final Image mImage;

    private final Context mContext;

    /**
     * The file we save the image into.
     */
    private final File mFile;

    public ImageSaver(Context context, Image image, File file) {
        mContext = context;
        mImage = image;
        mFile = new File(file.getPath());
    }

    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(mFile);
            output.write(bytes);
            scanFile(mFile);
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            mImage.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void scanFile(File file) {
        MediaScannerConnection.scanFile(mContext, new String[]{file.toString()}, null,
                (path, uri) -> {
                });
    }
}
