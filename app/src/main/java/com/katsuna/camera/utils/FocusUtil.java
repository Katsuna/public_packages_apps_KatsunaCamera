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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import timber.log.Timber;

public class FocusUtil {

    private static final String TAG = "FocusUtil";

    // https://gist.github.com/royshil/8c760c2485257c85a11cafd958548482
    public static MeteringRectangle getMeteringRect(View v, MotionEvent e, Rect sensorArraySize) {
        Timber.tag(TAG).d("getMeteringRect ...");

        int y = (int) ((e.getX() / (float) v.getWidth()) * (float) sensorArraySize.height());
        int x = (int) ((e.getY() / (float) v.getHeight()) * (float) sensorArraySize.width());
        int halfTouchWidth = 150;
        int halfTouchHeight = 150;

        return new MeteringRectangle(Math.max(x - halfTouchWidth, 0),
                Math.max(y - halfTouchHeight, 0),
                halfTouchWidth * 2,
                halfTouchHeight * 2,
                MeteringRectangle.METERING_WEIGHT_MAX - 1);
    }

    // https://stackoverflow.com/a/38537827
    public static void highlightFocusPoint(SurfaceView surfaceView, MotionEvent event) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);

        SurfaceHolder holder = surfaceView.getHolder();
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawColor(Color.TRANSPARENT);
                canvas.drawCircle(event.getX(), event.getY(), 100, paint);
                holder.unlockCanvasAndPost(canvas);
                new Handler().postDelayed(() -> {
                    Canvas canvas1 = holder.lockCanvas();
                    if (canvas1 != null) {
                        canvas1.drawColor(0, PorterDuff.Mode.CLEAR);
                        holder.unlockCanvasAndPost(canvas1);
                    }

                }, 1000);
            }
        }
    }

}
