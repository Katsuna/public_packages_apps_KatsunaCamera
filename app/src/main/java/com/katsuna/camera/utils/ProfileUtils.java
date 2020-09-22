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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.katsuna.camera.R;
import com.katsuna.commons.entities.ColorProfile;
import com.katsuna.commons.entities.ColorProfileKeyV2;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.ColorCalcV2;
import com.katsuna.commons.utils.DrawUtils;

public class ProfileUtils {

    public static void adjustCaptureButton(TextView button, UserProfile profile, Context context) {
        // color take button
        int takeButtonColor;
        if (profile.colorProfile == ColorProfile.CONTRAST) {
            takeButtonColor = ContextCompat.getColor(context, R.color.common_white);
        } else {
            takeButtonColor = ColorCalcV2.getColor(context, ColorProfileKeyV2.PRIMARY_COLOR_1,
                    profile.colorProfile);
        }

        Drawable[] drawables = button.getCompoundDrawablesRelative();
        for (Drawable dr : drawables) {
            if (dr instanceof LayerDrawable) {
                LayerDrawable layerDrawable = (LayerDrawable) dr;
                Drawable bgDrawable = layerDrawable.findDrawableByLayerId(R.id.capture_bg);
                DrawUtils.setColor(bgDrawable, takeButtonColor);
            }
        }
    }

    public static void enableRecMode(TextView button, boolean enabled, Context context) {
        Drawable[] drawables = button.getCompoundDrawablesRelative();
        for (Drawable dr : drawables) {
            if (dr instanceof LayerDrawable) {
                LayerDrawable layerDrawable = (LayerDrawable) dr;
                GradientDrawable bgDrawable = (GradientDrawable) layerDrawable
                        .findDrawableByLayerId(R.id.capture_on_bg);
                int takeButtonColor;
                if (enabled) {
                    takeButtonColor = ContextCompat.getColor(context, R.color.common_black);
                } else {
                    takeButtonColor = ContextCompat.getColor(context, R.color.common_transparent);
                }

                DrawUtils.setColor(bgDrawable, takeButtonColor);
            }
        }
    }
}
