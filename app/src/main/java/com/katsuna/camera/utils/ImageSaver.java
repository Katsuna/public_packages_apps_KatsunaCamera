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
