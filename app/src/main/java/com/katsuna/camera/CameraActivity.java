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

import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.katsuna.camera.data.BlackAndWhiteMode;
import com.katsuna.camera.data.FlashMode;
import com.katsuna.camera.data.SizeMode;
import com.katsuna.camera.data.source.SettingsDataSource;
import com.katsuna.camera.utils.DepedencyUtils;
import com.katsuna.camera.utils.OrientationManager;
import com.katsuna.camera.utils.OrientationManagerImpl;
import com.katsuna.camera.utils.StorageUtil;
import com.katsuna.commons.entities.ColorProfile;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.entities.UserProfileContainer;
import com.katsuna.commons.utils.BackgroundGenerator;
import com.katsuna.commons.utils.ProfileReader;
import com.katsuna.commons.utils.ToggleButtonAdjuster;

import java.lang.ref.WeakReference;
import java.util.List;

import timber.log.Timber;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class CameraActivity extends AppCompatActivity implements ICameraHost {

    private static final String CAMERA_ID_KEY = "CAMERA_ID_KEY";
    private static final String CAMERA_MODE_KEY = "CAMERA_MODE_KEY";

    private String mActiveCameraId;
    private CameraCharacteristics mActiveCameraCharacteristics;
    private CameraMode mActiveCameraMode;
    private View mSettings;
    private Button mSettingsClose;
    private ToggleButton mFlashToggle;
    private ToggleButton mSizeToggle;
    private ToggleButton mBlackWhiteToggle;
    private SettingsDataSource mSettingsDataSource;
    private UserProfile mUserProfile;
    private Handler mMainHandler;
    private OrientationManagerImpl mOrientationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSettings = findViewById(R.id.settings_layout);
        mSettings.setOnClickListener(v -> showSettings(false));
        mSettingsClose = findViewById(R.id.close_settings);
        mSettingsClose.setOnClickListener(v -> showSettings(false));

        mSettingsDataSource = DepedencyUtils.getSettingsDatasource(this);

        mFlashToggle = findViewById(R.id.toggle_flash);
        mSizeToggle = findViewById(R.id.toggle_photo_size);
        mBlackWhiteToggle = findViewById(R.id.toggle_black_and_white);

        View settingsContainer = findViewById(R.id.settings_container);
        settingsContainer.setOnClickListener(null);

        View info = findViewById(R.id.info);
        info.setOnClickListener(v -> {
            Intent i = new Intent(this, InfoActivity.class);
            startActivity(i);
        });

        initSettings();

        mFlashToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FlashMode newMode = isChecked ? FlashMode.AUTO : FlashMode.OFF;
            mSettingsDataSource.setFlashMode(newMode);
            refreshSettingsOnFragments();
        });

        mSizeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SizeMode newMode = isChecked ? SizeMode.SMALL : SizeMode.LARGE;
            mSettingsDataSource.setSizeMode(newMode);
            adjustPictureSize();
        });

        mBlackWhiteToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BlackAndWhiteMode newMode = isChecked ? BlackAndWhiteMode.ENABLED :
                    BlackAndWhiteMode.DISABLED;

            mSettingsDataSource.setBlackAndWhiteMode(newMode);
            refreshSettingsOnFragments();
        });

        if (savedInstanceState == null) {
            mActiveCameraMode = CameraMode.PICTURE;
            setFragment(PictureFragment.newInstance());
        } else {
            mActiveCameraId = savedInstanceState.getString(CAMERA_ID_KEY);
            mActiveCameraMode = (CameraMode) savedInstanceState.getSerializable(CAMERA_MODE_KEY);
            if (mActiveCameraMode == null) {
                mActiveCameraMode = CameraMode.PICTURE;
            }
            switch (mActiveCameraMode) {
                case PICTURE:
                    setFragment(PictureFragment.newInstance());
                    break;
                case VIDEO:
                    setFragment(VideoFragment.newInstance());
                    break;
            }
        }

        if (isCameraSecure()) {
            Timber.v("Starting in secure camera mode.");
            // show on lock screen
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        mMainHandler = new MainHandler(this, getMainLooper());
        mOrientationManager = new OrientationManagerImpl(this, mMainHandler);
    }

    @Override
    public UserProfile getUserProfile() {
        return mUserProfile;
    }

    @Override
    protected void onResume() {
        mOrientationManager.resume();

        super.onResume();
        refreshProfile();
        applyUserProfile();
    }

    @Override
    protected void onPause() {
        mOrientationManager.pause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mOrientationManager = null;

        super.onDestroy();
    }

    private void refreshProfile() {
        UserProfileContainer userProfileContainer = ProfileReader.getKatsunaUserProfile(this);
        mUserProfile = userProfileContainer.getActiveUserProfile();
    }

    private void applyUserProfile() {
        Drawable toggleBg;
        if (mUserProfile.colorProfile == ColorProfile.CONTRAST) {
            toggleBg = BackgroundGenerator.createToggleBgV3(this, mUserProfile, true);
        } else {
            toggleBg = BackgroundGenerator.createToggleBgV3(this, mUserProfile, false);
        }

        ToggleButtonAdjuster.adjustToggleButton(this, mFlashToggle, toggleBg, mUserProfile);
        ToggleButtonAdjuster.adjustToggleButton(this, mSizeToggle, toggleBg, mUserProfile);
        ToggleButtonAdjuster.adjustToggleButton(this, mBlackWhiteToggle, toggleBg, mUserProfile);
    }

    private void initSettings() {
        FlashMode flashMode = mSettingsDataSource.getFlashMode();
        mFlashToggle.setChecked(flashMode == FlashMode.AUTO);

        SizeMode sizeMode = mSettingsDataSource.getSizeMode();
        mSizeToggle.setChecked(sizeMode == SizeMode.SMALL);

        BlackAndWhiteMode blackAndWhiteMode = mSettingsDataSource.getBlackAndWhiteMode();
        mBlackWhiteToggle.setChecked(blackAndWhiteMode == BlackAndWhiteMode.ENABLED);
    }

    private void refreshSettingsOnFragments() {
        PictureFragment fragment = (PictureFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);

        if (fragment != null) {
            fragment.refreshSettings();
        }
    }

    private void adjustPictureSize() {
        PictureFragment fragment = (PictureFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);

        if (fragment != null) {
            fragment.reinitCamera();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(CAMERA_ID_KEY, mActiveCameraId);
        savedInstanceState.putSerializable(CAMERA_MODE_KEY, mActiveCameraMode);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void switchMode(CameraMode cameraMode) {
        Fragment newFragment = null;
        switch (cameraMode) {
            case PICTURE:
                mActiveCameraMode = CameraMode.VIDEO;
                newFragment = VideoFragment.newInstance();
                break;
            case VIDEO:
                mActiveCameraMode = CameraMode.PICTURE;
                newFragment = PictureFragment.newInstance();
                break;
        }

        setFragment(newFragment);
    }

    @Override
    public CameraMode getActiveCameraMode() {
        return mActiveCameraMode;
    }

    @Override
    public void goToGalleryApp(CameraMode cameraMode) {
        String bucketId = "";

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            bucketId = getBucketId();
        }

        Uri mediaUri;
        if (cameraMode == CameraMode.PICTURE) {
            mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        if (bucketId.length() > 0) {
            mediaUri = mediaUri.buildUpon()
                    .authority("media")
                    .appendQueryParameter("bucketId", bucketId)
                    .build();
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, mediaUri);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    // https://stackoverflow.com/a/35397245
    private String getBucketId() {
        String bucketId = "";

        final String[] projection = new String[]{"DISTINCT "
                + MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                + ", "
                + MediaStore.Images.Media.BUCKET_ID};

        final Cursor cur = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);

        while (cur != null && cur.moveToNext()) {
            int nameIndex = cur.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
            String bucketName = cur.getString(nameIndex);
            if (bucketName.equals(StorageUtil.KATSUNA_CAMERA)) {
                int buckedIdIndex = cur.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);
                bucketId = cur.getString(buckedIdIndex);
                break;
            }
        }

        if (cur != null) {
            cur.close();
        }

        return bucketId;
    }

    @Override
    public String getActiveCameraId() {
        return mActiveCameraId;
    }

    @Override
    public void setActiveCameraId(String cameraId) {
        mActiveCameraId = cameraId;
    }

    @Override
    public CameraCharacteristics getActiveCameraCharacteristics() {
        return mActiveCameraCharacteristics;
    }

    @Override
    public void setActiveCameraCharacteristics(CameraCharacteristics characteristics) {
        mActiveCameraCharacteristics = characteristics;
    }

    @Override
    public void showSettings(boolean enabled) {
        mSettings.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    @Override
    public void lockScreenRotation(boolean flag) {
        if (flag) {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        boolean shouldClose = true;

        // inform child fragments
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            if (f instanceof PictureFragment) {
                shouldClose = ((PictureFragment) f).onBackPressed();
                break;
            } else if (f instanceof VideoFragment) {
                shouldClose = ((VideoFragment) f).onBackPressed();
                break;
            }
        }

        if (shouldClose) {
            // pop them all
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            //finish();
            super.onBackPressed();
        }
    }

    // https://redmine.replicant.us/attachments/1095/0001-make-Camera-work-on-lock-screen-secure-mode.patch
    private boolean isKeyguardLocked() {
        KeyguardManager kgm = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        // isKeyguardSecure excludes the slide lock case.
        return (kgm != null) && kgm.isKeyguardLocked() && kgm.isKeyguardSecure();
    }

    private boolean isCameraSecure() {
        // Check if this is in the secure camera mode.
        String action = getIntent().getAction();
        if (Constants.ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action)
                || Constants.ACTION_IMAGE_CAPTURE_SECURE.equals(action)) {
            return true;
        } else {
            return isKeyguardLocked();
        }
    }

    @Override
    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }

    private static class MainHandler extends Handler {
        final WeakReference<CameraActivity> mActivity;

        MainHandler(CameraActivity activity, Looper looper) {
            super(looper);
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CameraActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            //noinspection StatementWithEmptyBody
            switch (msg.what) {

                // noop yet
/*                case MSG_CLEAR_SCREEN_ON_FLAG: {
                    if (!activity.mPaused) {
                        activity.getWindow().clearFlags(
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                    break;
                }*/
            }
        }
    }

}
