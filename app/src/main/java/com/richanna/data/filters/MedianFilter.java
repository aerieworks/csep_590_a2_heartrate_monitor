package com.richanna.data.filters;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProviderBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianFilter extends DataProviderBase<DataPoint> implements DataFilter<DataPoint, DataPoint> {

  private final int windowSize;
  private final List<DataPoint> dataPointWindow = new ArrayList<>();

  public MedianFilter(final int windowSize) {
    this.windowSize = windowSize;
  }

  @Override
  public void tell(DataPoint dataPoint) {
    dataPointWindow.add(dataPoint);
    if (dataPointWindow.size() >= windowSize) {
      while (dataPointWindow.size() > windowSize) {
        dataPointWindow.remove(0);
      }

      final float[] medianValues = new float[dataPointWindow.get(0).getValues().length];
      for (int i = 0; i < medianValues.length; i++) {
        medianValues[i] = calculateMedian(i);
      }

      provideDatum(new DataPoint(dataPoint.getTimestamp(), medianValues));
    }
  }

  private float calculateMedian(final int valueIndex) {
    final List<Float> values = new ArrayList<>(dataPointWindow.size());
    for (final DataPoint dataPoint : dataPointWindow) {
      values.add(dataPoint.getValues()[valueIndex]);
    }

    Collections.sort(values);
    return values.get(values.size() / 2);
  }
}
