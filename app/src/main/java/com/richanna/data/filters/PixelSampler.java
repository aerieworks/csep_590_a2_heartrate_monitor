package com.richanna.data.filters;

import android.util.Log;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;
import com.richanna.data.DataProviderBase;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class PixelSampler extends DataProviderBase<DataPoint<float[]>> implements DataFilter<CameraBridgeViewBase.CvCameraViewFrame, DataPoint<float[]>> {
  private static final String TAG = "PixelSampler";

  private final ImageSampleMode sampleMode;
  private final int sampleSize;

  public PixelSampler(final ImageSampleMode sampleMode, final int sampleSize, final DataProvider<CameraBridgeViewBase.CvCameraViewFrame> source) {
    this.sampleMode = sampleMode;
    this.sampleSize = sampleSize;
    source.addOnNewDatumListener(this);

    Log.i(TAG, String.format("Sampling mode: %s; size: %d", this.sampleMode.name(), this.sampleSize));
  }

  @Override
  public void tell(CameraBridgeViewBase.CvCameraViewFrame eventData) {
    final long timestamp = System.nanoTime();
    final Mat rgba = eventData.rgba();
    final Mat gray = eventData.gray();

    // This is a hacky but convenient way to pass both along to the next filters which can decide which to use.
    final float[] result = new float[eventData.rgba().channels() + eventData.gray().channels()];
    for (int i = 0; i < result.length; i++) {
      result[i] = 0;
    }

    final int numRows = eventData.rgba().rows();
    final int numCols = eventData.rgba().cols();
    int actualSampleSize = 0;
    if (sampleMode == ImageSampleMode.Center) {
      accumulatePixel(result, rgba, gray, numRows / 2, numCols / 2);
      actualSampleSize = 1;
    } else if (sampleMode == ImageSampleMode.Corners) {
      accumulatePixel(result, rgba, gray, 0, 0);
      accumulatePixel(result, rgba, gray, 0, numCols - 1);
      accumulatePixel(result, rgba, gray, numRows - 1, 0);
      accumulatePixel(result, rgba, gray, numRows - 1, numCols - 1);
      actualSampleSize = 4;
    } else if (sampleMode == ImageSampleMode.EvenDistribution) {
      final int rows = (int)Math.sqrt(sampleSize);
      final int cols = rows + (sampleSize % 2);
      final int rowStep = Math.max(0, Math.min(numRows - 1, numRows / (rows + 1)));
      final int colStep = Math.max(0, Math.min(numCols - 1, numCols / (cols + 1)));
      for (int row = rowStep; row < numRows; row += rowStep) {
        for (int col = colStep; col < numCols; col += colStep) {
          accumulatePixel(result, rgba, gray, row, col);
          actualSampleSize += 1;
        }
      }
    } else if (sampleMode == ImageSampleMode.Random) {
      for (int i = 0; i < sampleSize; i++) {
        accumulatePixel(result, rgba, gray, getRandomInteger(numRows), getRandomInteger(numCols));
        actualSampleSize += 1;
      }
    }

    for (int i = 0; i < result.length; i++) {
      result[i] = result[i] / (float)actualSampleSize;
    }

    provideDatum(new DataPoint<>(timestamp, result));
  }

  private void accumulatePixel(final float[] result, final Mat rgba, final Mat gray, final int row, final int col) {
    final double[] rgbaPixel = rgba.get(row, col);
    final double[] grayPixel = gray.get(row, col);

    for (int i = 0; i < rgbaPixel.length; i++) {
      result[i] += (float)rgbaPixel[i];
    }
    for (int i = 0; i < grayPixel.length; i++) {
      result[rgbaPixel.length + i] += (float)grayPixel[i];
    }
  }
  private int getRandomInteger(final int max) {
    return (int)(Math.random() * max);
  }

  public static enum ImageSampleMode {
    Center,
    Corners,
    EvenDistribution,
    Random
  }
}
