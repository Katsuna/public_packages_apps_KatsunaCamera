package com.katsuna.camera;

import android.hardware.camera2.CameraCharacteristics;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.katsuna.camera.api.CharacteristicUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
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
public class VideoCaptureTest {

    @Rule
    public ActivityTestRule<CameraActivity> mActivityRule =
            new ActivityTestRule<>(CameraActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule =
            GrantPermissionRule.grant(CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE);

    @Before
    public void start() {
    }

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

    private void sleep(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}