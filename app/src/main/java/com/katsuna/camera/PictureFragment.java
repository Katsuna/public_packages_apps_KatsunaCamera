package com.katsuna.camera;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.katsuna.camera.api.CameraHelper;
import com.katsuna.camera.api.CameraState;
import com.katsuna.camera.api.CharacteristicUtil;
import com.katsuna.camera.api.FlashUtil;
import com.katsuna.camera.data.BlackAndWhiteMode;
import com.katsuna.camera.data.FlashMode;
import com.katsuna.camera.data.SizeMode;
import com.katsuna.camera.data.source.SettingsDataSource;
import com.katsuna.camera.ui.AutoFitTextureView;
import com.katsuna.camera.ui.ConfirmationDialog;
import com.katsuna.camera.ui.ErrorDialog;
import com.katsuna.camera.ui.OnBackPressed;
import com.katsuna.camera.utils.CompareSizesByArea;
import com.katsuna.camera.utils.DepedencyUtils;
import com.katsuna.camera.utils.ImageSaver;
import com.katsuna.camera.utils.ProfileUtils;
import com.katsuna.camera.utils.SizeUtil;
import com.katsuna.camera.utils.StorageUtil;
import com.katsuna.commons.entities.UserProfile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static android.hardware.camera2.CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_INACTIVE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_TRIGGER_CANCEL;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_TRIGGER_START;
import static android.hardware.camera2.CameraMetadata.CONTROL_MODE_AUTO;
import static android.hardware.camera2.CaptureRequest.COLOR_CORRECTION_MODE;
import static android.hardware.camera2.CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER;
import static android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE;
import static android.hardware.camera2.CaptureRequest.CONTROL_AF_TRIGGER;
import static android.hardware.camera2.CaptureRequest.CONTROL_EFFECT_MODE;
import static android.hardware.camera2.CaptureRequest.CONTROL_MODE;
import static android.media.MediaActionSound.SHUTTER_CLICK;
import static com.katsuna.camera.Constants.CAMERA_PERMISSIONS;
import static com.katsuna.camera.Constants.REQUEST_CAMERA_PERMISSION;

public class PictureFragment extends Fragment implements OnBackPressed,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    private static final String FRAGMENT_DIALOG = "dialog";
    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "DDD";

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;
    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private SettingsDataSource mSettingsDatasource;
    private TextView mGalleryButton;
    private TextView mSwitchFacingButton;
    private TextView mSwitchModeButton;
    private ICameraHost mCameraHost;
    private CameraHelper mCameraHelper;
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;
    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;
    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;
    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;
    /**
     * This is the output file for our picture.
     */
    private File mFile;
    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            try {
                mFile = getFile();
                Timber.d("file to save: %s", mFile.toString());
                mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
            } catch (IOException ex) {
                handleCameraException(ex, CameraOperation.FILE_ACCESS);
            } finally {
                captureEnabled(true);
            }
        }

    };
    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;
    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;
    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private CameraState mState = CameraState.PREVIEW;
    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;
    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;
    private Size mCaptureSize;
    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Timber.tag(TAG).d("onSurfaceTextureAvailable");
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            Timber.tag(TAG).d("onSurfaceTextureSizeChanged");
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            Timber.tag(TAG).d("onSurfaceTextureDestroyed");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };
    private TextView mSettingsButton;
    private TextView mFlashButton;
    private boolean mAutoFocusSupported;
    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    Timber.tag(TAG).d("WAITING_LOCK %d", afState);
                    if (afState == null) {
                        runPrecaptureSequence();
                    } else if (afState == CONTROL_AF_STATE_INACTIVE ||
                            afState == CONTROL_AF_STATE_FOCUSED_LOCKED ||
                            afState == CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = CameraState.TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    } else {
                        captureStillPicture();
                    }
                    break;
                }
                case WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    Timber.tag(TAG).d("WAITING_PRECAPTURE %d", aeState);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_CONVERGED) {
                        mState = CameraState.WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    Timber.tag(TAG).d("WAITING_NON_PRECAPTURE %d", aeState);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = CameraState.TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Timber.tag(TAG).d("StateCallback : onOpened");
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Timber.tag(TAG).d("StateCallback : onDisconnected");
            mCameraOpenCloseLock.release();
            resetCameraDevice();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            Timber.tag(TAG).d("StateCallback : onError %d", error);
            mCameraOpenCloseLock.release();
            resetCameraDevice();
            finish();
        }

    };
    private TextView mTakeButton;

    public static PictureFragment newInstance() {
        return new PictureFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_picture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        mTakeButton = view.findViewById(R.id.take);
        mTakeButton.setOnClickListener(v -> takeButtonOnClick());
        mTextureView = view.findViewById(R.id.texture);

        initKatsunaControls(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        applyUserProfile();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        reopenCamera();
    }

    private void applyUserProfile() {
        UserProfile userProfile = mCameraHost.getUserProfile();
        ProfileUtils.adjustCaptureButton(mTakeButton, userProfile, getContext());
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            ConfirmationDialog.newInstance(R.string.request_permission, CAMERA_PERMISSIONS,
                    REQUEST_CAMERA_PERMISSION).show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void finish() {
        Activity activity = Objects.requireNonNull(getActivity());
        activity.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 2
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                showError(R.string.request_permission);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        try {
            String cameraId = mCameraHost.getActiveCameraId();
            CameraCharacteristics characteristics = mCameraHelper.getCameraCharacteristics(cameraId);

            List<Size> captureSizes = CharacteristicUtil.getCaptureOutputSizes(characteristics);
            // For still image captures, we use the largest available size.
            mCaptureSize = SizeUtil.getCaptureSize(captureSizes, new CompareSizesByArea());
            Timber.tag(TAG).d("max capture size: %s", mCaptureSize);

            adjustPictureSize();

            // Find out if we need to swap dimension to get the preview size relative to sensor
            // coordinate.
            int displayRotation = Objects.requireNonNull(activity).getWindowManager()
                    .getDefaultDisplay().getRotation();

            //noinspection ConstantConditions
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Timber.tag(TAG).e("Display rotation is invalid: %s", displayRotation);
            }

            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }

            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.

            List<Size> previewSizes = CharacteristicUtil.getPreviewOutputSizes(characteristics);
            mPreviewSize = SizeUtil.chooseOptimalSize(previewSizes, rotatedPreviewWidth,
                    rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, mCaptureSize);

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(
                        mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(
                        mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }

            // Check if the flash is supported.
            mFlashSupported = CharacteristicUtil.isFlashSupported(characteristics);
        } catch (NullPointerException e) {
            showError(R.string.camera_api_not_supported);
        }
    }

    private void openCamera(int width, int height) {
        Timber.tag(TAG).d("openCamera width %d, height %d", width, height);
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = mCameraHost.getActiveCameraId();
            mCameraHelper.openCamera(cameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException | InterruptedException ex) {
            handleCameraException(ex, CameraOperation.OPEN_CAMERA);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        Timber.tag(TAG).d("closeCamera");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }

            resetCameraDevice();

            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void resetCameraDevice() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
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
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            List<Surface> outputs = Arrays.asList(surface, mImageReader.getSurface());
            mCameraDevice.createCaptureSession(outputs, getPreviewStateCallback(), null);
        } catch (CameraAccessException ex) {
            handleCameraException(ex, CameraOperation.PREVIEW_CREATION);
        }
    }

    private CameraCaptureSession.StateCallback getPreviewStateCallback() {
        return new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                // The camera is already closed
                if (null == mCameraDevice) {
                    return;
                }

                // When the session is ready, we start displaying the preview.
                mCaptureSession = cameraCaptureSession;
                try {
                    enableDefaultModes(mPreviewRequestBuilder);

                    // Finally, we start displaying the camera preview.
                    mPreviewRequest = mPreviewRequestBuilder.build();
                    mCaptureSession.setRepeatingRequest(mPreviewRequest,
                            mCaptureCallback, mBackgroundHandler);
                } catch (CameraAccessException ex) {
                    handleCameraException(ex, CameraOperation.PREVIEW_CREATION);
                }
            }

            @Override
            public void onConfigureFailed(
                    @NonNull CameraCaptureSession cameraCaptureSession) {
                Toast.makeText(getContext(), R.string.preview_configuration_failed,
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void enableDefaultModes(CaptureRequest.Builder builder) {
        if (builder == null) return;

        builder.set(CONTROL_MODE, CONTROL_MODE_AUTO);

        CameraCharacteristics characteristics = mCameraHost.getActiveCameraCharacteristics();
        mAutoFocusSupported = CharacteristicUtil.isContinuousAutoFocusSupported(characteristics);
        if (mAutoFocusSupported) {
            builder.set(CONTROL_AF_MODE, CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        } else {
            builder.set(CONTROL_AF_MODE, CONTROL_AF_MODE_AUTO);
        }

        builder.set(COLOR_CORRECTION_MODE, COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);

        if (mFlashSupported) {
            FlashUtil.adjustRequestBuilder(builder, mSettingsDatasource.getFlashMode());
        }
        addBlackAndWhiteMode(builder, mSettingsDatasource.getBlackAndWhiteMode());
    }

    public void refreshSettings() {
        FlashMode flashMode = mSettingsDatasource.getFlashMode();
        FlashUtil.applyFlashMode(mFlashButton, flashMode);

        enableDefaultModes(mPreviewRequestBuilder);
        updatePreview();
    }

    public void reinitCamera() {
        closeCamera();
        reopenCamera();
    }

    public void adjustPictureSize() {
        SizeMode sizeMode = mSettingsDatasource.getSizeMode();

        int divider = 1;
        if (sizeMode == SizeMode.SMALL) {
            divider = 2;
        }
        int adjustedWidth = mCaptureSize.getWidth() / divider;
        int adjustedHeight = mCaptureSize.getHeight() / divider;

        mImageReader = ImageReader.newInstance(adjustedWidth, adjustedHeight, ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
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
        Matrix matrix = SizeUtil.getMatrix(viewWidth, viewHeight, mPreviewSize, rotation);
        mTextureView.setTransform(matrix);
    }

    /**
     * Initiate a still image capture.
     */
    private void takePicture() {
        // init file location
        mFile = null;

        if (mAutoFocusSupported) {
            lockFocus();
        } else {
            captureStillPicture();
        }
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CONTROL_AF_TRIGGER, CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = CameraState.WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            handleCameraException(e, CameraOperation.LOCK_FOCUS);
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private void runPrecaptureSequence() {
        try {
            Timber.tag(TAG).d("runPrecaptureSequence");
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CONTROL_AE_PRECAPTURE_TRIGGER,
                    CONTROL_AE_PRECAPTURE_TRIGGER_START);

            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = CameraState.WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException ex) {
            handleCameraException(ex, CameraOperation.PRECAPTURE);
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        try {
            Timber.tag(TAG).d("captureStillPicture");
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
            captureBuilder.addTarget(mImageReader.getSurface());

            CameraHelper.cloneBuilder(mPreviewRequestBuilder, captureBuilder);

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback captureCallback =
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                       @NonNull CaptureRequest request,
                                                       @NonNull TotalCaptureResult result) {
                            try {
                                playShutterSound();
                                File fileToSave = getFile();
                                if (fileToSave != null) {
                                    Toast.makeText(getContext(), R.string.picture_taken,
                                            Toast.LENGTH_SHORT).show();
                                    Timber.tag(TAG).d(mFile.toString());
                                }
                            } catch (IOException ex) {
                                handleCameraException(ex, CameraOperation.FILE_ACCESS);
                            }

                            unlockFocus();
                        }
                    };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            handleCameraException(e, CameraOperation.CAPTURE_STILL_PICTURE);
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CONTROL_AF_TRIGGER, CONTROL_AF_TRIGGER_CANCEL);
            // Reset the auto-exposure trigger
            mPreviewRequestBuilder.set(CONTROL_AE_PRECAPTURE_TRIGGER,
                    CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL);

            mPreviewRequest = mPreviewRequestBuilder.build();
            mCaptureSession.capture(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = CameraState.PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException ex) {
            handleCameraException(ex, CameraOperation.UNLOCK_FOCUS);
        }
    }

    private boolean mCaptureEnabled = true;

    private void takeButtonOnClick() {
        if (!mCaptureEnabled) {
            Timber.d("mCaptureEnabled not enabled");
            return;
        }

        if (CharacteristicUtil.camera2Supported(mCameraHost.getActiveCameraCharacteristics())) {
            // check for space available
            if (StorageUtil.hasAvailableSpaceToCapturePicture()) {
                captureEnabled(false);
                takePicture();
            } else {
                Toast.makeText(getContext(), R.string.not_available_space_for_picture,
                        Toast.LENGTH_LONG).show();
            }
        } else {
            showError(R.string.camera_api_not_supported);
        }
    }

    private void addBlackAndWhiteMode(CaptureRequest.Builder builder, BlackAndWhiteMode bwMode) {
        if (bwMode == BlackAndWhiteMode.ENABLED) {
            builder.set(CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
        } else {
            builder.set(CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_OFF);
        }
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
        mSettingsButton.setOnClickListener(v -> mCameraHost.showSettings(true));

        mSwitchFacingButton = view.findViewById(R.id.switch_facing_button);
        mSwitchFacingButton.setOnClickListener(v -> switchCamera());

        mFlashButton = view.findViewById(R.id.flash_button);
        mFlashButton.setOnClickListener(v -> switchFlash());

        // draw proper icon
        FlashMode flashMode = mSettingsDatasource.getFlashMode();
        FlashUtil.applyFlashMode(mFlashButton, flashMode);

        mGalleryButton = view.findViewById(R.id.gallery_button);
        mGalleryButton.setOnClickListener(v -> mCameraHost.goToGalleryApp());

        mSwitchModeButton = view.findViewById(R.id.switch_mode);
        mSwitchModeButton.setOnClickListener(v -> mCameraHost.switchMode(CameraMode.PICTURE));

        mMediaActionSound = new MediaActionSound();
        mMediaActionSound.load(SHUTTER_CLICK);
    }

    private CameraActivity getCameraActivity() {
        Activity activity = getActivity();
        return (CameraActivity) activity;
    }

    private void switchCamera() {
        String activeCameraId = mCameraHost.getActiveCameraId();
        String nextCameraId = mCameraHelper.getNextCameraId(activeCameraId);
        if (!activeCameraId.equals(nextCameraId)) {
            mCameraHost.setActiveCameraId(nextCameraId);
            mCameraHost.setActiveCameraCharacteristics(
                    mCameraHelper.getCameraCharacteristics(nextCameraId));
        }
        closeCamera();
        reopenCamera();
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
        FlashUtil.adjustRequestBuilder(mPreviewRequestBuilder, flashMode);
        updatePreview();
    }


    private void updatePreview() {
        if (mCaptureSession == null || mPreviewRequestBuilder == null) return;

        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException ex) {
            handleCameraException(ex, CameraOperation.PREVIEW_UPDATE);
        }
    }


    private void reopenCamera() {
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public boolean onBackPressed() {
        Timber.d("onBackPressed");
        return true;
    }

    private File getFile() throws IOException {
        if (mFile == null) {
            mFile = StorageUtil.getPhotoFilePath();
        }
        return mFile;
    }

    private void handleCameraException(Exception e, CameraOperation op) {
        Timber.e(e, "Operation %s", op);
        captureEnabled(true);

        Integer messageResId = null;
        switch (op) {
            case OPEN_CAMERA:
                messageResId = R.string.camera_open_failed;
                break;
            case PREVIEW_CREATION:
                messageResId = R.string.preview_creation_failed;
                break;
            case PREVIEW_UPDATE:
                messageResId = R.string.preview_update_failed;
                break;
            case LOCK_FOCUS:
                messageResId = R.string.lock_focus_failed;
                break;
            case PRECAPTURE:
                messageResId = R.string.capture_picture_failed;
                break;
            case CAPTURE_STILL_PICTURE:
                messageResId = R.string.capture_picture_failed;
                break;
            case FILE_ACCESS:
                messageResId = R.string.file_access_failed;
                break;
            case UNLOCK_FOCUS:
                messageResId = R.string.unlock_focus_failed;
                break;
        }

        if (messageResId != null) {
            showError(messageResId);
        }
    }

    private void captureEnabled(boolean enabled) {
        Timber.d("captureEnabled %s", enabled);
        mCaptureEnabled = enabled;
    }

    private void showError(int messageResId) {
        String message = getString(messageResId);
        ErrorDialog.newInstance(message).show(getChildFragmentManager(), FRAGMENT_DIALOG);
    }

    private MediaActionSound mMediaActionSound;

    private void playShutterSound() {
        mMediaActionSound .play(SHUTTER_CLICK);
    }

}
