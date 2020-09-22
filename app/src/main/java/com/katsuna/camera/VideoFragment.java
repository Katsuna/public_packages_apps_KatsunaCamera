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

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaActionSound;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.katsuna.camera.api.CameraHelper;
import com.katsuna.camera.api.CharacteristicUtil;
import com.katsuna.camera.api.FlashUtil;
import com.katsuna.camera.data.FlashMode;
import com.katsuna.camera.data.source.SettingsDataSource;
import com.katsuna.camera.ui.AutoFitTextureView;
import com.katsuna.camera.ui.ConfirmationDialog;
import com.katsuna.camera.ui.ErrorDialog;
import com.katsuna.camera.ui.OnBackPressed;
import com.katsuna.camera.utils.CameraUtil;
import com.katsuna.camera.utils.ChronometerUtils;
import com.katsuna.camera.utils.CompareSizesByArea;
import com.katsuna.camera.utils.DepedencyUtils;
import com.katsuna.camera.utils.ProfileUtils;
import com.katsuna.camera.utils.StorageUtil;
import com.katsuna.commons.entities.ColorProfile;
import com.katsuna.commons.entities.ColorProfileKeyV2;
import com.katsuna.commons.entities.UserProfile;
import com.katsuna.commons.utils.ColorCalcV2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.media.MediaActionSound.START_VIDEO_RECORDING;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static com.katsuna.camera.Constants.REQUEST_VIDEO_PERMISSIONS;
import static com.katsuna.camera.Constants.VIDEO_PERMISSIONS;

public class VideoFragment extends Fragment implements
        ActivityCompat.OnRequestPermissionsResultCallback, OnBackPressed {

    private static final String TAG = "VideoFragment";

    private static final String FRAGMENT_DIALOG = "dialog";

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;
    /**
     * Button to record video
     */
    private TextView mButtonVideo;
    /**
     * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;
    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;
    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;
    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;
    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;
    private String mNextVideoAbsolutePath;
    private CaptureRequest.Builder mPreviewBuilder;
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    private CameraHelper mCameraHelper;
    private TextView mSwitchModeButton;
    private ICameraHost mCameraHost;
    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };
    private TextView mGalleryButton;
    private TextView mSettingsButton;
    private TextView mFlashButton;
    private SettingsDataSource mSettingsDatasource;
    private Chronometer mChronometer;

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Timber.tag(TAG).e("Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Timber.tag(TAG).e("Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        mTextureView = view.findViewById(R.id.texture);

        initKatsunaControls(view);
    }

    private void initKatsunaControls(View view) {
        mCameraHelper = new CameraHelper(view.getContext());
        mCameraHost = getCameraActivity();

        mSettingsDatasource = DepedencyUtils.getSettingsDatasource(view.getContext());

        // set active camera if not set
        String cameraId = mCameraHost.getActiveCameraId();
        if (cameraId == null) {
            cameraId = mCameraHelper.getInitialCameraId();
        }
        mCameraHost.setActiveCameraId(cameraId);
        mCameraHost.setActiveCameraCharacteristics(mCameraHelper.getCameraCharacteristics(cameraId));

        mSettingsButton = view.findViewById(R.id.settings_button);
        showCameraInfo();

        mFlashButton = view.findViewById(R.id.flash_button);

        FlashMode flashMode = mSettingsDatasource.getFlashMode();
        FlashUtil.applyFlashMode(mFlashButton, flashMode);

        mFlashButton.setOnClickListener(v -> switchFlash());

        mGalleryButton = view.findViewById(R.id.gallery_button);
        mGalleryButton.setOnClickListener(v -> mCameraHost.goToGalleryApp(CameraMode.VIDEO));

        mSwitchModeButton = view.findViewById(R.id.switch_mode);
        mSwitchModeButton.setOnClickListener(v -> {
                    if (!mIsRecordingVideo) {
                        mCameraHost.switchMode(CameraMode.VIDEO);
                    } else {
                        Toast.makeText(getContext(),
                                R.string.switch_to_photo_failed_video_is_recording,
                                Toast.LENGTH_LONG).show();
                    }
                }
        );

        // adjust for video
        mSwitchModeButton.setText(R.string.switch_to_photo);
        Drawable dr = ContextCompat.getDrawable(getCameraActivity(), R.drawable.ic_photo_camera);
        mSwitchModeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, dr, null, null);

        mChronometer = view.findViewById(R.id.chronometer);
        ChronometerUtils.reset(mChronometer);
        mButtonVideo = view.findViewById(R.id.take);
        mButtonVideo.setText(R.string.record_video);
        mButtonVideo.setOnClickListener(v -> recordButtonOnClick());

        mMediaActionSound = new MediaActionSound();
        mMediaActionSound.load(START_VIDEO_RECORDING);
    }

    private void switchFlash() {
        FlashMode currentFlashMode = mSettingsDatasource.getFlashMode();
        FlashMode flashMode = FlashMode.defaultMode();
        if (currentFlashMode == FlashMode.ON) {
            flashMode = FlashMode.AUTO;
        } else if (currentFlashMode == FlashMode.AUTO) {
            flashMode = FlashMode.OFF;
        } else if (currentFlashMode == FlashMode.OFF) {
            flashMode = FlashMode.ON;
        }
        Timber.tag(TAG).d("mFlashMode is %s", flashMode);

        // store new setting
        mSettingsDatasource.setFlashMode(flashMode);
        FlashUtil.applyFlashMode(mFlashButton, flashMode);

        updatePreview();
    }

    private void showCameraInfo() {
        CameraMode mode = mCameraHost.getActiveCameraMode();
        String info = String.format("cId: %s mode: %s", mCameraHost.getActiveCameraId(), mode);
        if (mSettingsButton != null) {
            mSettingsButton.setText(info);
        }
    }

    private CameraActivity getCameraActivity() {
        Activity activity = getActivity();
        return (CameraActivity) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        applyUserProfile();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        resetStorageHandler();
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void applyUserProfile() {
        UserProfile userProfile = mCameraHost.getUserProfile();
        ProfileUtils.adjustCaptureButton(mButtonVideo, userProfile, getContext());

        int pColor1;
        if (userProfile.colorProfile == ColorProfile.CONTRAST) {
            pColor1 = Color.WHITE;
        } else {
            pColor1 = ColorCalcV2.getColor(getContext(), ColorProfileKeyV2.PRIMARY_COLOR_1,
                    userProfile.colorProfile);
        }
        mChronometer.setTextColor(pColor1);
    }

    private void recordButtonOnClick() {
        if (CharacteristicUtil.camera2Supported(mCameraHost.getActiveCameraCharacteristics())) {
            if (mIsRecordingVideo) {
                stopRecordingVideo();
            } else {
                // check for space available
                if (StorageUtil.storageReady()) {
                    if (StorageUtil.hasAvailableSpaceToRecordVideo()) {
                        startRecordingVideo();
                    } else {
                        Toast.makeText(getContext(), R.string.not_available_space_for_video,
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.external_storage_unavailable,
                            Toast.LENGTH_LONG).show();
                }
            }
        } else {
            showError(R.string.camera_api_not_supported);
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException ex) {
            Timber.e(ex);
        }
    }

    /**
     * Gets whether you should show UI with rationale for requesting permissions.
     *
     * @param permissions The permissions your app wants to request.
     * @return Whether you can show permission rationale UI.
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requests permissions needed for recording video.
     */
    private void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            ConfirmationDialog.newInstance(R.string.request_permission, VIDEO_PERMISSIONS,
                    REQUEST_VIDEO_PERMISSIONS).show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Timber.tag(TAG).d("onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PERMISSION_GRANTED) {
                        showError(R.string.permission_request);
                        break;
                    }
                }
            } else {
                showError(R.string.permission_request);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (checkSelfPermission(Objects.requireNonNull(getContext()), permission)
                    != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
     */
    @SuppressWarnings("MissingPermission")
    private void openCamera(int width, int height) {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions();
            return;
        }
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        try {
            Timber.tag(TAG).d("tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = mCameraHost.getActiveCameraId();

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = mCameraHelper.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(width, height);
            mMediaRecorder = new MediaRecorder();
            mCameraHelper.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException | InterruptedException ex) {
            handleCameraException(ex, CameraOperation.OPEN_CAMERA);
            activity.finish();
        } catch (NullPointerException ex) {
            handleCameraException(ex, CameraOperation.ACCESS_CAMERA_INFO);
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Start the camera preview.
     */
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(getContext(), R.string.preview_configuration_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException ex) {
            handleCameraException(ex, CameraOperation.PREVIEW_CREATION);
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException ex) {
            handleCameraException(ex, CameraOperation.PREVIEW_UPDATE);
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void setUpMediaRecorder() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath();
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        // Orientation
        CameraCharacteristics characteristics = mCameraHost.getActiveCameraCharacteristics();

        @SuppressWarnings("ConstantConditions")
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

        int deviceOrientation = mCameraHost.getOrientationManager().getDeviceOrientation()
                .getDegrees();

        boolean isLensFacing = CharacteristicUtil.isLensFacing(characteristics);

        int rotation = CameraUtil.getImageRotation(sensorOrientation, deviceOrientation,
                isLensFacing);

        Timber.d("mMediaRecorder.setOrientationHint %d", rotation);
        mMediaRecorder.setOrientationHint(rotation);


        mMediaRecorder.prepare();
    }

    private String getVideoFilePath() throws IOException {
        final File dir = StorageUtil.getVideoFilePath();
        return dir.getAbsolutePath();
    }

    private void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    FlashMode flashMode = mSettingsDatasource.getFlashMode();
                    FlashUtil.adjustRequestBuilder4Video(mPreviewBuilder, flashMode);
                    updatePreview();
                    Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                        // UI
                        mButtonVideo.setText(R.string.stop);
                        ProfileUtils.enableRecMode(mButtonVideo, true, getContext());
                        mIsRecordingVideo = true;

                        // Start recording
                        mCameraHost.lockScreenRotation(true);
                        startChronometer();
                        mMediaRecorder.start();

                        playShutterSound();
                        // check storage every 5secs if there is available space left
                        storageHandler.postDelayed(checkStorageRunnable, 5000);
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getContext(), R.string.configuration_failed, Toast.LENGTH_SHORT)
                            .show();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException ex) {
            handleCameraException(ex, CameraOperation.CAPTURE_VIDEO);
        }

    }

    private void startChronometer() {
        ChronometerUtils.adjustFormat(mChronometer);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    private void stopChronometer() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
        ChronometerUtils.reset(mChronometer);
    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    private void stopRecordingVideo() {
        mCameraHost.lockScreenRotation(false);
        stopChronometer();
        // UI
        mIsRecordingVideo = false;
        mButtonVideo.setText(R.string.record_video);
        ProfileUtils.enableRecMode(mButtonVideo, false, getContext());

        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        Toast.makeText(getContext(), R.string.video_recorded, Toast.LENGTH_SHORT).show();
        Timber.tag(TAG).d("Video saved: %s ", mNextVideoAbsolutePath);

        MediaScannerConnection.scanFile(getContext(), new String[]{mNextVideoAbsolutePath}, null,
                (path, uri) -> {
                });

        mNextVideoAbsolutePath = null;
        startPreview();
    }

    @Override
    public boolean onBackPressed() {
        Timber.d("onBackPressed");
        return !mIsRecordingVideo;
    }

    private void handleCameraException(Exception e, CameraOperation op) {
        Timber.e(e, "Operation %s", op);

        Integer messageResId = null;
        switch (op) {
            case OPEN_CAMERA:
                messageResId = R.string.camera_open_failed;
                break;
            case ACCESS_CAMERA_INFO:
                messageResId = R.string.camera_api_not_supported;
                break;
            case PREVIEW_CREATION:
                messageResId = R.string.preview_creation_failed;
                break;
            case PREVIEW_UPDATE:
                messageResId = R.string.preview_update_failed;
                break;
            case CAPTURE_VIDEO:
                messageResId = R.string.capture_video_failed;
                break;
        }

        if (messageResId != null) {
            showError(messageResId);
        }
    }

    private void showError(int messageResId) {
        String message = getString(messageResId);
        ErrorDialog.newInstance(message).show(getChildFragmentManager(), FRAGMENT_DIALOG);
    }

    private final Handler storageHandler = new Handler();

    private void resetStorageHandler() {
        storageHandler.removeCallbacks(checkStorageRunnable);
    }

    private final Runnable checkStorageRunnable = new Runnable() {
        public void run() {
            try {
                if (StorageUtil.hasAvailableSpaceToRecordVideo()) {
                    // try again after 5secs
                    storageHandler.postDelayed(this, 5000);
                } else {
                    stopRecordingVideo();
                    Toast.makeText(getContext(), R.string.recording_stopped_due_storage,
                            Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    };

    private MediaActionSound mMediaActionSound;

    private void playShutterSound() {
        mMediaActionSound.play(START_VIDEO_RECORDING);
    }
}
