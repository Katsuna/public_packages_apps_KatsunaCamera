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
package com.katsuna.camera.data.source;

import android.support.annotation.NonNull;

import com.katsuna.camera.data.BlackAndWhiteMode;
import com.katsuna.camera.data.FlashMode;
import com.katsuna.camera.data.SizeMode;

public interface SettingsDataSource {

    @NonNull
    FlashMode getFlashMode();

    void setFlashMode(@NonNull FlashMode flashMode);

    @NonNull
    SizeMode getSizeMode();

    void setSizeMode(@NonNull SizeMode sizeMode);

    @NonNull
    BlackAndWhiteMode getBlackAndWhiteMode();

    void setBlackAndWhiteMode(@NonNull BlackAndWhiteMode blackAndWhiteMode);

}
