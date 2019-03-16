package com.katsuna.camera;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.katsuna.camera.data.BlackAndWhiteMode;
import com.katsuna.camera.data.FlashMode;
import com.katsuna.camera.data.SizeMode;
import com.katsuna.camera.data.source.SettingsDataSource;
import com.katsuna.camera.utils.DepedencyUtils;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.entities.UserProfileContainer;
import com.katsuna.commons.utils.BackgroundGenerator;
import com.katsuna.commons.utils.ProfileReader;
import com.katsuna.commons.utils.ToggleButtonAdjuster;

import java.util.List;

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
    }

    @Override
    public UserProfile getUserProfile() {
        return mUserProfile;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfile();
        applyUserProfile();
    }

    private void refreshProfile() {
        UserProfileContainer userProfileContainer = ProfileReader.getKatsunaUserProfile(this);
        mUserProfile = userProfileContainer.getActiveUserProfile();
    }

    private void applyUserProfile() {
        Drawable toggleBg = BackgroundGenerator.createToggleBgV3(this, mUserProfile, false);
        ToggleButtonAdjuster.adjustToggleButton(this, mFlashToggle, toggleBg, mUserProfile);
        ToggleButtonAdjuster.adjustToggleButton(this, mSizeToggle, toggleBg, mUserProfile);
        ToggleButtonAdjuster.adjustToggleButton(this, mBlackWhiteToggle, toggleBg, mUserProfile);
    }

    private void initSettings() {
        FlashMode flashMode = mSettingsDataSource.getFlashMode();
        mFlashToggle.setChecked(flashMode == FlashMode.AUTO);

        SizeMode sizeMode = mSettingsDataSource.getSizeMode();
        mSizeToggle.setChecked(sizeMode == SizeMode.LARGE);

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
            fragment.adjustPictureSize();
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
    public void goToGalleryApp() {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.app_not_found, Toast.LENGTH_SHORT).show();
        }
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
}
