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
import android.support.annotation.NonNull;

/**
 * Initializable singleton for providing the application level context
 * object instead of initializing each singleton separately.
 */
@SuppressWarnings("ALL")
public class AndroidContext {
    private static AndroidContext sInstance;

    /**
     * The android context object cannot be created until the android
     * has created the application object. The AndroidContext object
     * must be initialized before other singletons can use it.
     */
    public static void initialize(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new AndroidContext(context);
        }
    }

    /**
     * Return a previously initialized instance, throw if it has not been
     * initialized yet.
     */
    public static AndroidContext instance() {
        if (sInstance == null) {
            throw new IllegalStateException("Android context was not initialized.");
        }
        return sInstance;
    }

    private final Context mContext;
    private AndroidContext(Context context) {
        mContext = context;
    }

    public Context get() {
        return mContext;
    }
}