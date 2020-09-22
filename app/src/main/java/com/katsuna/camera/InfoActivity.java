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

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.ImageView;
import android.widget.TextView;

import com.katsuna.commons.controls.KatsunaNavigationView;
import com.katsuna.commons.ui.KatsunaActivity;
import com.katsuna.commons.utils.BrowserUtils;

import timber.log.Timber;

import static com.katsuna.commons.utils.Constants.KATSUNA_PRIVACY_URL;
import static com.katsuna.commons.utils.Constants.KATSUNA_TERMS_OF_USE;

public class InfoActivity extends KatsunaActivity {

    private static final String TAG = InfoActivity.class.getSimpleName();

    private TextView mAppName;
    private TextView mAppVersion;
    private ImageView mAppIcon;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        mAppName = findViewById(com.katsuna.commons.R.id.app_name);
        mAppVersion = findViewById(com.katsuna.commons.R.id.app_version);
        mAppIcon = findViewById(com.katsuna.commons.R.id.app_icon);

        initControls();
    }

    @Override
    protected void showPopup(boolean flag) {
        // no op
    }

    private void initControls() {
        initToolbar(R.drawable.common_ic_close_black54_24dp);

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            mAppIcon.setImageResource(R.mipmap.ic_camera_launcher);
            mAppName.setText(R.string.app_name);
            mAppVersion.setText(getString(R.string.common_version_info, pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e("%s %s", TAG, e.getMessage());
        }

        initDrawer();
    }

    private void initDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.common_navigation_drawer_open,
                R.string.common_navigation_drawer_close);
        assert mDrawerLayout != null;
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupDrawerLayout();
    }

    private void setupDrawerLayout() {
        KatsunaNavigationView navigationView = findViewById(R.id.katsuna_navigation_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    mDrawerLayout.closeDrawers();

                    switch (menuItem.getItemId()) {
                        case R.id.drawer_settings:
                            startActivity(new Intent(InfoActivity.this, SettingsActivity.class));
                            break;
                        case R.id.drawer_privacy:
                            BrowserUtils.openUrl(InfoActivity.this, KATSUNA_PRIVACY_URL);
                            break;
                        case R.id.drawer_terms:
                            BrowserUtils.openUrl(InfoActivity.this, KATSUNA_TERMS_OF_USE);
                            break;
                    }

                    return true;
                });
        navigationView.setOnClickListener(v -> mDrawerLayout.closeDrawers());
    }

}