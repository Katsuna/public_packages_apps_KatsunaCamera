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
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
public class VideoCaptureTest {

    @Rule
    public ActivityTestRule<CameraActivity> mActivityRule =
            new ActivityTestRule<>(CameraActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule =
            GrantPermissionRule.grant(CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE);

    @Test
    public void clickRecordVideo_showsSuccessMessageOrNotSupportedDevice() {

        // wait for camera to init properly
        sleep(1);

        CameraCharacteristics chars = mActivityRule.getActivity().getActiveCameraCharacteristics();

        // switch to video
        onView(withId(R.id.switch_mode)).perform(click());

        // wait for camera switch to video properly
        sleep(1);

        // start recording video
        onView(withId(R.id.take)).perform(click());

        if (CharacteristicUtil.camera2Supported(chars)) {
            // record for 2
            sleep(2);

            // stop recording video
            onView(withId(R.id.take)).perform(click());

            // verify message
            onView(withText(R.string.video_recorded))
                    .inRoot(withDecorView(not(mActivityRule.getActivity().getWindow().getDecorView())))
                    .check(matches(isDisplayed()));
        } else {
            onView(withText(R.string.camera_api_not_supported)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickRecordVideoWithFlashOn_showsSuccessMessageOrNotSupportedDevice() {

        // set flash on
        Context context = getInstrumentation().getTargetContext();
        SettingsPreferenceDataSource prefDatasource = new SettingsPreferenceDataSource(context);
        prefDatasource.setFlashMode(FlashMode.ON);

        // wait for camera to init properly
        sleep(1);

        CameraCharacteristics chars = mActivityRule.getActivity().getActiveCameraCharacteristics();

        // switch to video
        onView(withId(R.id.switch_mode)).perform(click());

        // wait for camera switch to video properly
        sleep(1);

        // start recording video
        onView(withId(R.id.take)).perform(click());

        if (CharacteristicUtil.camera2Supported(chars)) {
            // record for 2
            sleep(2);

            // stop recording video
            onView(withId(R.id.take)).perform(click());

            // verify message
            onView(withText(R.string.video_recorded))
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