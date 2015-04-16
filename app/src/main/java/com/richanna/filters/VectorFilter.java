package com.richanna.filters;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;

public class VectorFilter implements DataFilter {

  private final int vectorIndex;
  private int index = 0;

  public VectorFilter(final int vectorIndex) {
    this.vectorIndex = vectorIndex;
  }

  @Override
  public DataPoint apply(DataPoint dataPoint) {
    final float[] values = dataPoint.getValues();
    if (vectorIndex < values.length) {
      final DataPoint result = new DataPoint(dataPoint.getTimestamp(), new float[] { index, values[vectorIndex] });
      index += 1;
      return result;
    }

    return null;
  }
}
