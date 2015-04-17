package com.richanna.data.filters;

import android.util.Log;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProviderBase;

import java.util.ArrayList;
import java.util.List;

public class MeanShifter extends DataProviderBase<DataPoint> implements DataFilter<DataPoint, DataPoint> {

  private static final String TAG = "MeanShifter";

  private final int windowSize;
  private final List<DataPoint> dataPointWindow = new ArrayList<>();
  private int emitCount = 0;
  private float[] currentMean = null;

  public MeanShifter(final int windowSize) {
    this.windowSize = windowSize;
  }

  @Override
  public void tell(DataPoint dataPoint) {
    dataPointWindow.add(dataPoint);
    if (dataPointWindow.size() >= windowSize) {
      while (dataPointWindow.size() > windowSize) {
        dataPointWindow.remove(0);
      }

      final DataPoint source = dataPointWindow.get(windowSize / 2);
      if (emitCount == 0) {
        currentMean = new float[source.getValues().length];
        for (int i = 0; i < currentMean.length; i++) {
          currentMean[i] = calculateMean(i);
        }
        Log.d(TAG, String.format("New mean: %f", currentMean[0]));
      }
      emitCount = (emitCount + 1) % (windowSize / 2);

      final float[] adjustedValues = new float[source.getValues().length];
      for (int i = 0; i < adjustedValues.length; i++) {
          adjustedValues[i] = source.getValues()[i] - currentMean[i];
      }
      provideDatum(new DataPoint(dataPoint.getTimestamp(), adjustedValues));
    }
  }

  private float calculateMean(final int valueIndex) {
    float total = 0;
    for (final DataPoint dataPoint : dataPointWindow) {
      total += dataPoint.getValues()[valueIndex];
    }

    return total / (float)dataPointWindow.size();
  }
}
