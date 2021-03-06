/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.libre.mixtli;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Size;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.libre.mixtli.env.ImageUtils;
import com.libre.mixtli.env.Logger;
import com.libre.mixtli.ui.AddContactFragment;
import com.libre.mixtli.ui.MapMaskFragment;
import com.unstoppable.submitbuttonview.SubmitButton;
import java.nio.ByteBuffer;

public abstract class CameraActivity extends AppCompatActivity
    implements OnImageAvailableListener, Camera.PreviewCallback,ActivityCompat.OnRequestPermissionsResultCallback {
  private static final Logger LOGGER = new Logger();

  private static final int PERMISSIONS_REQUEST = 1;

  private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
  private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
  private static final String PERMISSION_WIFI_STATE = Manifest.permission.ACCESS_WIFI_STATE;
  private static final String PERMISSION_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
  private static final String PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
  private static final String PERMISSION_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;


  private boolean debug = false;
  private FragmentManager fragmentManager;
  private FragmentTransaction fragmentTransaction;
  private Handler handler;
  private HandlerThread handlerThread;
  private boolean useCamera2API;
  private boolean isProcessingFrame = false;
  private byte[][] yuvBytes = new byte[3][];
  private int[] rgbBytes = null;
  private int yRowStride;

  protected int previewWidth = 0;
  protected int previewHeight = 0;

  private Runnable postInferenceCallback;
  private Runnable imageConverter;

  private SubmitButton btnDemo;
  private SubmitButton btnInicio;
  private ImageView btnSettings;
  private LinearLayout lytPrincipal,lytDemo;
  private ImageView gunOn,gunOff;
  private boolean startDemo=false;
  final Animation animation = new AlphaAnimation((float) 0.5, 0);
  final private Fragment fragmentMap=new MapMaskFragment();
  final private Fragment fragmentContact=new AddContactFragment();
  private ImageButton contactButton;
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    LOGGER.d("onCreate " + this);
    super.onCreate(null);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    fragmentManager=getFragmentManager();
    fragmentTransaction=fragmentManager.beginTransaction();
    setContentView(R.layout.activity_camera);
    Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
    myToolbar.setTitle("");
    contactButton=(ImageButton)myToolbar.findViewById(R.id.new_contact);
    setSupportActionBar(myToolbar);

    contactButton.setOnClickListener(contactListener);
    btnDemo=(SubmitButton) findViewById(R.id.btnDemo);
    btnDemo.setOnClickListener(demoListener);
    btnInicio=(SubmitButton) findViewById(R.id.btnInicar);
    btnInicio.setOnClickListener(startListener);
    lytPrincipal=(LinearLayout) findViewById(R.id.lytPrincipal);
    lytDemo=(LinearLayout) findViewById(R.id.lytDemo);
    gunOn=(ImageView) findViewById(R.id.gunOn);
    gunOff=(ImageView) findViewById(R.id.gunOff);

    animation.setDuration(500);
    animation.setInterpolator(new LinearInterpolator());
    animation.setRepeatCount(Animation.INFINITE);
    animation.setRepeatMode(Animation.REVERSE);

    if (hasPermission()) {
      setFragment();
    } else {
      requestPermission();
    }
  }

  @Override
  public void onBackPressed(){

    fragmentTransaction = fragmentManager.beginTransaction();
    if(fragmentMap.isVisible()) {
      fragmentTransaction.remove(fragmentMap).commit();
    }else if(fragmentContact.isVisible()){
      fragmentTransaction.remove(fragmentContact).commit();
    }else {
      setPrincipal();
    }


  }
  private byte[] lastPreviewFrame;

  protected int[] getRgbBytes() {
    imageConverter.run();
    return rgbBytes;
  }

  protected int getLuminanceStride() {
    return yRowStride;
  }

  protected byte[] getLuminance() {
    return yuvBytes[0];
  }

  /**
   * Callback for android.hardware.Camera API
   */
  @Override
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      LOGGER.w("Dropping frame!");
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        rgbBytes = new int[previewWidth * previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      return;
    }

    isProcessingFrame = true;
    lastPreviewFrame = bytes;
    yuvBytes[0] = bytes;
    yRowStride = previewWidth;

    imageConverter =
        new Runnable() {
          @Override
          public void run() {
            ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
          }
        };

    postInferenceCallback =
        new Runnable() {
          @Override
          public void run() {
            camera.addCallbackBuffer(bytes);
            isProcessingFrame = false;
          }
        };

        if(startDemo) {
          processImage();
        }
  }

  /**
   * Callback for Camera2 API
   */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    //We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);
      yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
          new Runnable() {
            @Override
            public void run() {
              ImageUtils.convertYUV420ToARGB8888(
                  yuvBytes[0],
                  yuvBytes[1],
                  yuvBytes[2],
                  previewWidth,
                  previewHeight,
                  yRowStride,
                  uvRowStride,
                  uvPixelStride,
                  rgbBytes);
            }
          };

      postInferenceCallback =
          new Runnable() {
            @Override
            public void run() {
              image.close();
              isProcessingFrame = false;
            }
          };

      processImage();
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  @Override
  public synchronized void onStart() {
    LOGGER.d("onStart " + this);
    super.onStart();
  }

  @Override
  public synchronized void onResume() {
    LOGGER.d("onResume " + this);
    super.onResume();

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public synchronized void onPause() {
    LOGGER.d("onPause " + this);

    if (!isFinishing()) {
      LOGGER.d("Requesting finish");
      finish();
    }

    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }

    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    LOGGER.d("onStop " + this);
    super.onStop();
  }

  @Override
  public synchronized void onDestroy() {
    LOGGER.d("onDestroy " + this);
    super.onDestroy();
  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      final int requestCode, final String[] permissions, final int[] grantResults) {
    if (requestCode == PERMISSIONS_REQUEST) {
      if (grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED
          && grantResults[1] == PackageManager.PERMISSION_GRANTED
          && grantResults[2] == PackageManager.PERMISSION_GRANTED
          && grantResults[3] == PackageManager.PERMISSION_GRANTED
          && grantResults[4] == PackageManager.PERMISSION_GRANTED
          && grantResults[5] == PackageManager.PERMISSION_GRANTED
              ) {
        setFragment();
      } else {
        requestPermission();
      }
    }
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return  checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED &&
              checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED&&
              checkSelfPermission(PERMISSION_WIFI_STATE) == PackageManager.PERMISSION_GRANTED&&
              checkSelfPermission(PERMISSION_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED&&
              checkSelfPermission(PERMISSION_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED&&
              checkSelfPermission(PERMISSION_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (
              shouldShowRequestPermissionRationale(PERMISSION_CAMERA) ||
              shouldShowRequestPermissionRationale(PERMISSION_STORAGE)||
              shouldShowRequestPermissionRationale(PERMISSION_WIFI_STATE)||
              shouldShowRequestPermissionRationale(PERMISSION_NETWORK_STATE)||
              shouldShowRequestPermissionRationale(PERMISSION_FINE_LOCATION)||
              shouldShowRequestPermissionRationale(PERMISSION_COARSE_LOCATION)
              ) {
        Toast.makeText(CameraActivity.this,
            "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
      }
      requestPermissions(new String[] {
              PERMISSION_CAMERA,
              PERMISSION_STORAGE,
              PERMISSION_WIFI_STATE,
              PERMISSION_NETWORK_STATE,
              PERMISSION_FINE_LOCATION,
              PERMISSION_COARSE_LOCATION
      }, PERMISSIONS_REQUEST);
    }
  }

  // Returns true if the device supports the required hardware level, or better.
  private boolean isHardwareLevelSupported(
      CameraCharacteristics characteristics, int requiredLevel) {
    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
      return requiredLevel == deviceLevel;
    }
    // deviceLevel is not LEGACY, can use numerical sort
    return requiredLevel <= deviceLevel;
  }

  private String chooseCamera() {
    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // We don't use a front facing camera in this sample.
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        final StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
          continue;
        }

        // Fallback to camera1 API for internal cameras that don't have full support.
        // This should help with legacy situations where using the camera2 API causes
        // distorted or otherwise broken previews.
        useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
            || isHardwareLevelSupported(characteristics, 
                                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
        LOGGER.i("Camera API lv2?: %s", useCamera2API);
        return cameraId;
      }
    } catch (CameraAccessException e) {
      LOGGER.e(e, "Not allowed to access camera");
    }

    return null;
  }

  protected void setFragment() {
    String cameraId = chooseCamera();

    Fragment fragment;
    if (useCamera2API) {
      CameraConnectionFragment camera2Fragment =
          CameraConnectionFragment.newInstance(
              new CameraConnectionFragment.ConnectionCallback() {
                @Override
                public void onPreviewSizeChosen(final Size size, final int rotation) {
                  previewHeight = size.getHeight();
                  previewWidth = size.getWidth();
                  CameraActivity.this.onPreviewSizeChosen(size, rotation);
                }
              },
              this,
              getLayoutId(),
              getDesiredPreviewFrameSize());

      camera2Fragment.setCamera("1");
      fragment = camera2Fragment;
    } else {
      fragment =
          new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
    }

    getFragmentManager()
        .beginTransaction()
        .replace(R.id.container, fragment)
        .commit();
  }

  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  public void requestRender() {
    final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
    if (overlay != null) {
      overlay.postInvalidate();
    }
  }

  public void addCallback(final OverlayView.DrawCallback callback) {
    final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
    if (overlay != null) {
      overlay.addCallback(callback);
    }
  }

  public void onSetDebug(final boolean debug) {}

  @Override
  public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      debug = !debug;
      requestRender();
      onSetDebug(debug);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  protected void readyForNextImage() {
    if (postInferenceCallback != null) {
      postInferenceCallback.run();
    }
  }

  protected int getScreenOrientation() {
    switch (getWindowManager().getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_270:
        return 270;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_90:
        return 90;
      default:
        return 0;
    }
  }

  protected abstract void processImage();
  protected abstract void onPreviewSizeChosen(final Size size, final int rotation);
  protected abstract int getLayoutId();
  protected abstract Size getDesiredPreviewFrameSize();

  public void setGunAllert(){
    this.runOnUiThread(new Runnable() {
      public void run() {
        gunOff.setVisibility(View.GONE);
        gunOn.setVisibility(View.VISIBLE);
        gunOn.startAnimation(animation);
      }
    });


  }

  View.OnClickListener contactListener=new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      setAddContact();
    }
  };

  View.OnClickListener demoListener=new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      setDemoView();
    }
  };

  View.OnClickListener startListener=new View.OnClickListener() {
    @Override
    public void onClick(View v) {
       setMask();
    }
  };
  public void setMask(){
    setDemoView();
    gunOn.setVisibility(View.INVISIBLE);
    gunOff.setVisibility(View.INVISIBLE);
    fragmentTransaction
            .replace(R.id.container_mask,fragmentMap)
            .commit();
  }
  public void setAddContact(){
    fragmentTransaction
            .replace(R.id.container_mask,fragmentContact)
            .commit();
  }
  private void setDemoView(){
    lytPrincipal.setVisibility(View.GONE);
    startDemo=true;
    processImage();
    lytDemo.setVisibility(View.VISIBLE);
  }
  private void setPrincipal(){
    lytPrincipal.setVisibility(View.VISIBLE);
    gunOn.setVisibility(View.INVISIBLE);
    gunOff.setVisibility(View.VISIBLE);
    btnDemo.reset();
    btnInicio.reset();
    startDemo=false;
    processImage();
    lytDemo.setVisibility(View.GONE);
  }


}
