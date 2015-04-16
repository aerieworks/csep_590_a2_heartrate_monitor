package com.richanna.filters;

import android.util.Log;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;

import java.util.ArrayList;
import java.util.List;

public class MeanShifter implements DataFilter {

  private final int windowSize;
  private final List<DataPoint> dataPointWindow = new ArrayList<>();
  private int emitCount = 0;
  private float[] currentMean = null;

  public MeanShifter(final int windowSize) {
    this.windowSize = windowSize;
  }

  @Override
  public DataPoint apply(DataPoint dataPoint) {
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
        Log.d("MeanShifter", String.format("New mean: %f, %f, %f", currentMean[0], currentMean[1], currentMean[2]));
      }
      emitCount = (emitCount + 1) % (windowSize / 2);

      final float[] adjustedValues = new float[] {
          source.getValues()[0] - currentMean[0],
          source.getValues()[1] - currentMean[1],
          source.getValues()[2] - currentMean[2]
      };
      return new DataPoint(dataPoint.getTimestamp(), adjustedValues);
    }

    return null;
  }

  private float calculateMean(final int valueIndex) {
    float total = 0;
    for (final DataPoint dataPoint : dataPointWindow) {
      total += dataPoint.getValues()[valueIndex];
    }

    return total / (float)dataPointWindow.size();
  }
}
