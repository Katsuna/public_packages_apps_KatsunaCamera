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

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.katsuna.camera.api.CharacteristicUtil;
import com.katsuna.camera.data.FlashMode;
import com.katsuna.camera.data.source.SettingsPreferenceDataSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
public class PictureCaptureTest {

    @Rule
    public ActivityTestRule<CameraActivity> mActivityRule =
            new ActivityTestRule<>(CameraActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule =
            GrantPermissionRule.grant(CAMERA, WRITE_EXTERNAL_STORAGE);

    @Test
    public void clickTakePicture_showsSuccessMessageOrNotSupportedDevice() {

        // wait for camera to init properly
        sleep(1);

        // Click on the take picture button
        onView(withId(R.id.take)).perform(click());

        checkValidResponses();
    }

    @Test
    public void switchCameraAndTakePicture_showsSuccessMessageOrNotSupportedDevice() {

        // wait for camera to init properly
        sleep(1);

        // switch camera
        onView(withId(R.id.switch_facing_button)).perform(click());

        sleep(1);

        // Click on the take picture button
        onView(withId(R.id.take)).perform(click());

        checkValidResponses();
    }

    @Test
    public void toggleBWSettingAndTakePicture_showsSuccessMessageOrNotSupportedDevice() {

        // wait for camera to init properly
        sleep(1);

        // open settings
        onView(withId(R.id.settings_button)).perform(click());

        // toggle bw setting
        onView(withId(R.id.toggle_black_and_white)).perform(click());

        // close settings
        onView(withId(R.id.close_settings)).perform(click());

        // Click on the take picture button
        onView(withId(R.id.take)).perform(click());

        checkValidResponses();
    }

    @Test
    public void toggleSizeSettingAndTakePicture_showsSuccessMessageOrNotSupportedDevice() {

        // wait for camera to init properly
        sleep(1);

        // open settings
        onView(withId(R.id.settings_button)).perform(click());

        // toggle bw setting
        onView(withId(R.id.toggle_photo_size)).perform(click());

        // close settings
        onView(withId(R.id.close_settings)).perform(click());

        // wait for camera to init properly
        sleep(1);

        // Click on the take picture button
        onView(withId(R.id.take)).perform(click());

        checkValidResponses();
    }

    @Test
    public void takePictureWithFlashOn_showsSuccessMessageOrNotSupportedDevice() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SettingsPreferenceDataSource prefDatasource = new SettingsPreferenceDataSource(context);
        prefDatasource.setFlashMode(FlashMode.ON);

        // wait for camera to init properly
        sleep(1);

        // Click on the take picture button
        onView(withId(R.id.take)).perform(click());

        checkValidResponses();
    }

    @Test
    public void takePictureWithFlashAuto_showsSuccessMessageOrNotSupportedDevice() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SettingsPreferenceDataSource prefDatasource = new SettingsPreferenceDataSource(context);
        prefDatasource.setFlashMode(FlashMode.AUTO);

        // wait for camera to init properly
        sleep(1);

        // Click on the take picture button
        onView(withId(R.id.take)).perform(click());

        checkValidResponses();
    }

    private void checkValidResponses() {
        CameraCharacteristics chars = mActivityRule.getActivity().getActiveCameraCharacteristics();
        if (CharacteristicUtil.camera2Supported(chars)) {
            onView(withText(R.string.picture_taken))
                    .inRoot(withDecorView(not(mActivityRule.getActivity().getWindow().getDecorView())))
                    .check(matches(isDisplayed()));
        } else {
            onView(withText(R.string.camera_api_not_supported)).check(matches(isDisplayed()));
        }
    }

    private void sleep(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}