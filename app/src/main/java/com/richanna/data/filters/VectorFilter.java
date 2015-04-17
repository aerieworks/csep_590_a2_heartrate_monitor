package com.richanna.data.filters;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProviderBase;

public class VectorFilter extends DataProviderBase<DataPoint> implements DataFilter<DataPoint, DataPoint> {

  private final int vectorIndex;
  private int index = 0;

  public VectorFilter(final int vectorIndex) {
    this.vectorIndex = vectorIndex;
  }

  @Override
  public void tell(DataPoint dataPoint) {
    final float[] values = dataPoint.getValues();
    if (vectorIndex < values.length) {
      final DataPoint result = new DataPoint(dataPoint.getTimestamp(), new float[] { index, values[vectorIndex] });
      index += 1;
      provideDatum(result);
    }
  }
}
