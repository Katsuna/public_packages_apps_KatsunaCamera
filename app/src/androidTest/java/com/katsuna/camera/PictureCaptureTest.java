package com.katsuna.camera;

import android.hardware.camera2.CameraCharacteristics;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import com.katsuna.camera.api.CharacteristicUtil;

import org.junit.Before;
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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class PictureCaptureTest {

    private UiDevice mDevice;

    @Rule
    public ActivityTestRule<CameraActivity> mActivityRule =
            new ActivityTestRule<>(CameraActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule =
            GrantPermissionRule.grant(CAMERA, WRITE_EXTERNAL_STORAGE);


    @Before
    public void start() {
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void checkPreconditions() {
        assertThat(mDevice, notNullValue());
    }

    @Test
    public void clickTakePicture_showsSuccessMessageOrNotSupportedDevice() {

        // wait for camera to init properly
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        CameraCharacteristics chars = mActivityRule.getActivity().getActiveCameraCharacteristics();

        // Click on the take picture button
        onView(withId(R.id.take)).perform(click());

        if (CharacteristicUtil.camera2Supported(chars)) {
            onView(withText(R.string.picture_taken))
                    .inRoot(withDecorView(not(mActivityRule.getActivity().getWindow().getDecorView())))
                    .check(matches(isDisplayed()));
        } else {
            onView(withText(R.string.camera_api_not_supported)).check(matches(isDisplayed()));
        }
    }
}