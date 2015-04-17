package com.richanna.data.filters;

import android.util.Log;

import com.badlogic.gdx.audio.analysis.FFT;
import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProviderBase;

import java.util.ArrayList;
import java.util.List;

public class FftFilter extends DataProviderBase<DataPoint> implements DataFilter<DataPoint, DataPoint> {

  private final int windowSize;
  private final List<DataPoint> dataPoints = new ArrayList<>();

  public FftFilter(final int windowSize) {
    this.windowSize = windowSize;
  }

  @Override
  public void tell(DataPoint eventData) {
    dataPoints.add(eventData);
    if (dataPoints.size() == windowSize) {
      computeFft();
      while (dataPoints.size() > windowSize * 0.75) {
        dataPoints.remove(0);
      }
    }
  }

  private void computeFft() {
    final long timestamp = System.nanoTime();
    final float[] samples = new float[windowSize];
    for (int i = 0; i < windowSize; i++) {
      samples[i] = dataPoints.get(i).getValues()[0];
    }

    final FFT fft = new FFT(windowSize, 18);
    fft.forward(samples);

    fft.getSpectrum();
    final float[] real = fft.getRealPart();
    final float[] imaginary = fft.getImaginaryPart();

    float max = 0;
    int maxIndex = 0;
    for (int i = 0; i < windowSize; i++) {
      final float magnitude = (float)Math.sqrt((real[i] * real[i]) + (imaginary[i] * imaginary[i]));
      if (magnitude > max) {
        max = magnitude;
        maxIndex = i;
      }
      provideDatum(new DataPoint(timestamp, new float[] { magnitude }));
    }
    Log.d("FftFilter", String.format("Maximum: %d, %f", maxIndex, max));
  }
}
