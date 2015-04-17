package com.richanna.data.filters;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProviderBase;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class IntensityFilter extends DataProviderBase<DataPoint> implements DataFilter<CameraBridgeViewBase.CvCameraViewFrame, DataPoint> {

  @Override
  public void tell(final CameraBridgeViewBase.CvCameraViewFrame eventData) {
    //final Mat frame = eventData.gray();
    //final double intensity = frame.get(frame.rows() / 2, frame.cols() / 2)[0];
    final Mat frame = eventData.rgba();
    final double[] values = frame.get(frame.rows() / 2, frame.cols() / 2);
    double intensity = 0;
    for (int i = 0; i < values.length; i++) {
      intensity += values[i];
    }

    provideDatum(new DataPoint(System.nanoTime(), new float[] { (float)intensity / (float)values.length }));
  }
}
