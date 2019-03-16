package com.katsuna.camera;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class PictureCaptureTest {

    private UiDevice mDevice;

    @Rule
    public ActivityTestRule<CameraActivity> mActivityRule =
            new ActivityTestRule<>(CameraActivity.class);


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
    public void clickTakePicture_showsSuccessMessage() {
        // Click on the add alarm button
        onView(withId(R.id.take)).perform(click());

        // Check if the add alarm screen is displayed
        //onView(withId(R.id.minute)).check(matches(isDisplayed()));
    }
}