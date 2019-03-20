package com.katsuna.camera;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import com.katsuna.camera.api.CharacteristicUtil;
import com.katsuna.camera.data.FlashMode;
import com.katsuna.camera.data.source.SettingsPreferenceDataSource;

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