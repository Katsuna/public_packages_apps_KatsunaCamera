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

import android.os.Bundle;
import android.view.ViewGroup;

import com.katsuna.commons.entities.ColorProfile;
import com.katsuna.commons.entities.SizeProfile;
import com.katsuna.commons.ui.SettingsActivityBase;
import com.katsuna.commons.utils.SizeAdjuster;

public class SettingsActivity extends SettingsActivityBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initControls();
    }

    @Override
    protected void onResume() {
        super.onResume();

        applyProfiles();
    }

    @Override
    protected void applyColorProfile(ColorProfile colorProfile) {
        // no op here
    }

    @Override
    protected void applySizeProfile(SizeProfile sizeProfile) {
        ViewGroup topViewGroup = findViewById(android.R.id.content);
        SizeAdjuster.applySizeProfile(this, topViewGroup, sizeProfile);
    }

    protected void initControls() {
        super.initControls();
        initToolbar();
        mScrollViewContainer = findViewById(R.id.scroll_view_container);
    }
}