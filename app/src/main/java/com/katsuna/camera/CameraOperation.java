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
package com.katsuna.camera;

public enum CameraOperation {
    OPEN_CAMERA,
    ACCESS_CAMERA_INFO,
    PREVIEW_CREATION,
    PREVIEW_UPDATE,
    LOCK_FOCUS,
    PRECAPTURE,
    CAPTURE_STILL_PICTURE,
    CAPTURE_VIDEO,
    FILE_ACCESS,
    UNLOCK_FOCUS,
    MANUAL_FOCUS
}
