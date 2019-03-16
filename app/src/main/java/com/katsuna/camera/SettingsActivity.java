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