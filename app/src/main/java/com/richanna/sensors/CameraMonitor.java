package com.richanna.sensors;

import android.util.Log;

import com.richanna.data.DataGenerator;
import com.richanna.data.DataProviderBase;
import com.richanna.heartratemonitor.CameraView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class CameraMonitor extends DataProviderBase<CameraBridgeViewBase.CvCameraViewFrame> implements DataGenerator<CameraBridgeViewBase.CvCameraViewFrame>, CameraBridgeViewBase.CvCameraViewListener2 {

  private static final String TAG = "CameraMonitor";

  private final CameraView cameraView;

  public CameraMonitor(final CameraView cameraView) {
    this.cameraView = cameraView;
    cameraView.setCvCameraViewListener(this);
  }

  @Override
  public void onCameraViewStarted(int width, int height) {
    Log.d(TAG, "View started");
    cameraView.enableFlash();
  }

  @Override
  public void onCameraViewStopped() {
    Log.d(TAG, "View stopped");
    cameraView.disableFlash();
  }

  @Override
  public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
    provideDatum(inputFrame);
    return inputFrame.rgba();
  }

  @Override
  public void pause() {
    Log.d(TAG, "pausing");
   cameraView.disableView();
  }

  @Override
  public void resume() {
    Log.d(TAG, "Resuming");
    cameraView.enableView();
  }
}
