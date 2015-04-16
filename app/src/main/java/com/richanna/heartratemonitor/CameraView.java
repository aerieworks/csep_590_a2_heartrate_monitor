package com.richanna.heartratemonitor;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.util.List;


/**
 * TODO: document your custom view class.
 */
public class CameraView extends JavaCameraView {

  public CameraView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void enableFlash() {
    setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
  }

  public void disableFlash() {
    setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
  }

  private void setFlashMode(final String mode) {
    if (mCamera != null) {
      Log.d("CameraView", "Enabling flash");
      final Camera.Parameters p = mCamera.getParameters();
      final List<String> supportedModes = p.getSupportedFlashModes();
      if (supportedModes != null && supportedModes.contains(mode)) {
        p.setFlashMode(mode);
        mCamera.setParameters(p);
      } else {
        Log.d("CameraView", String.format("Flash mode %s not supported", mode));
      }
    }
  }
}
