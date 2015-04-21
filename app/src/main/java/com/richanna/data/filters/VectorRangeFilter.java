package com.richanna.data.filters;

import android.util.Log;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;
import com.richanna.data.DataProviderBase;

public class VectorRangeFilter extends DataProviderBase<DataPoint<float[]>> implements DataFilter<DataPoint<float[]>, DataPoint<float[]>> {

  private static final String TAG = "VectorRangeFilter";

  private final int rangeStart;
  private final int rangeEnd;

  public VectorRangeFilter(final int rangeStart, final int rangeEnd, DataProvider<DataPoint<float[]>> source) {
    this.rangeStart = rangeStart;
    this.rangeEnd = rangeEnd;
    source.addOnNewDatumListener(this);

    Log.i(TAG, String.format("Range start: %d; end: %d", this.rangeStart, this.rangeEnd));
  }

  @Override
  public void tell(DataPoint<float[]> dataPoint) {
    final float[] values = dataPoint.getValue();
    final float[] result = new float[1 + rangeEnd - rangeStart];
    for (int i = 0; i < result.length; i++) {
      final int valueIndex = rangeStart + i;
      result[i] = (valueIndex < values.length ? values[valueIndex] : 0);
    }

    provideDatum(new DataPoint<>(dataPoint.getTimestamp(), result));
  }
}
