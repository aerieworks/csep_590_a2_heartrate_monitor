package com.richanna.data.filters;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;
import com.richanna.data.DataProviderBase;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class PixelSampler extends DataProviderBase<DataPoint<float[]>> implements DataFilter<CameraBridgeViewBase.CvCameraViewFrame, DataPoint<float[]>> {
  private final int channels;
  private final int sampleSize;

  public PixelSampler(final int channels, final int sampleSize, final DataProvider<CameraBridgeViewBase.CvCameraViewFrame> source) {
    this.channels = channels;
    this.sampleSize = sampleSize;
    source.addOnNewDatumListener(this);
  }

  @Override
  public void tell(CameraBridgeViewBase.CvCameraViewFrame eventData) {
    final Mat frame = eventData.rgba();
    final long timestamp = System.nanoTime();

    final float[] result = new float[channels];
    for (int i = 0; i < channels; i++) {
      result[i] = 0;
    }

    /*for (int i = 0; i < sampleSize; i++) {
      final int row = getRandomInteger(1);//frame.rows());
      final int column = getRandomInteger(1);//frame.cols());
    }*/
    accumulatePixel(result, frame, 0, 0);
    accumulatePixel(result, frame, 0, frame.cols() - 1);
    accumulatePixel(result, frame, frame.rows() - 1, 0);
    accumulatePixel(result, frame, frame.rows() - 1, frame.cols() - 1);

    for (int i = 0; i < channels; i++) {
      result[i] = result[i] / (float)sampleSize;
    }

    provideDatum(new DataPoint<>(timestamp, result));
  }

  private void accumulatePixel(final float[] result, final Mat frame, final int row, final int column) {
    final double[] pixel = frame.get(row, column);

    for (int j = 0; j < channels; j++) {
      result[j] += (float)pixel[j];
    }

  }
  private int getRandomInteger(final int max) {
    return (int)(Math.random() * max);
  }
}
