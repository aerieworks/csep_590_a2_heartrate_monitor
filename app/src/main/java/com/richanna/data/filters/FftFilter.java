package com.richanna.data.filters;

import android.util.Log;

import com.badlogic.gdx.audio.analysis.FFT;
import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProviderBase;

import java.util.ArrayList;
import java.util.List;

public class FftFilter extends DataProviderBase<DataPoint> implements DataFilter<DataPoint, DataPoint> {

  private static final String TAG = "FftFilter";

  private final List<DataPoint> dataPoints = new ArrayList<>();
  private final FrequencyCalculator frequencyCalculator = new FrequencyCalculator();
  private final int windowSize;

  public FftFilter(final int windowSize) {
    this.windowSize = windowSize;
  }

  public FrequencyCalculator getFrequencyCalculator() { return frequencyCalculator; }

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

    final float[] magnitude = new float[windowSize / 2];
    for (int i = 0; i < magnitude.length; i++) {
      magnitude[i] = (float)Math.sqrt((real[i] * real[i]) + (imaginary[i] * imaginary[i]));
      provideDatum(new DataPoint(timestamp, new float[] { magnitude[i] }));
    }

    final float timespan = (float)(dataPoints.get(dataPoints.size() - 1).getTimestamp() - dataPoints.get(0).getTimestamp())/1000000000f;
    frequencyCalculator.updateFrequency(timespan, windowSize, magnitude);
  }

  private static class FrequencyCalculator extends DataProviderBase<Float> {

    private void updateFrequency(final float timespan, final int sampleSize, final float[] magnitude) {
      int maxIndex = 0;
      for (int i = 0; i < magnitude.length; i++) {
        if (magnitude[i] > magnitude[maxIndex]) {
          maxIndex = i;
        }
      }

      final float sampleRate = (float)sampleSize / timespan;
      final float observableRate = sampleRate / 2.0f;
      final float frequency = (float)maxIndex * (observableRate / (float)magnitude.length);
      Log.d(TAG, String.format("Frequency: [%f] (%d, %f): %f == %f", observableRate, maxIndex, magnitude[maxIndex], frequency, frequency * 60.0f));
      provideDatum(frequency);
    }
  }
}
