package com.richanna.data.filters;

import android.util.Log;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;
import com.richanna.data.DataProviderBase;

public class VectorFilter extends DataProviderBase<DataPoint<Float>> implements DataFilter<DataPoint<float[]>, DataPoint<Float>> {

  private static final String TAG = "VectorFilter";

  private final int vectorIndex;

  public VectorFilter(final int vectorIndex, final DataProvider<DataPoint<float[]>> source) {
    this.vectorIndex = vectorIndex;
    source.addOnNewDatumListener(this);

    Log.i(TAG, String.format("Vector index: %d", this.vectorIndex));
  }

  @Override
  public void tell(DataPoint<float[]> dataPoint) {
    final float[] values = dataPoint.getValue();
    if (vectorIndex < values.length) {
      final DataPoint result = new DataPoint<>(dataPoint.getTimestamp(), values[vectorIndex]);
      provideDatum(result);
    }
  }
}
