package com.richanna.sensors;

import android.util.Log;

import com.richanna.data.DataGenerator;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class CameraMonitor extends DataGenerator<CameraBridgeViewBase.CvCameraViewFrame> implements CameraBridgeViewBase.CvCameraViewListener2 {

  private final CameraBridgeViewBase cameraView;

  public CameraMonitor(final CameraBridgeViewBase cameraView) {
    this.cameraView = cameraView;
    cameraView.setCvCameraViewListener(this);
  }

  @Override
  public void onCameraViewStarted(int width, int height) {
    // Do nothing.
  }

  @Override
  public void onCameraViewStopped() {
    // Do nothing.
  }

  @Override
  public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
    Log.d("CameraMonitor", "Got frame");
    return null;
  }

  @Override
  public void pause() {
    cameraView.disableView();
  }

  @Override
  public void resume() {
    cameraView.enableView();
  }
}
