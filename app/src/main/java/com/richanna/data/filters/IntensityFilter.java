package com.richanna.data.filters;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;
import com.richanna.data.DataProviderBase;

public class IntensityFilter extends DataProviderBase<DataPoint<Float>> implements DataFilter<DataPoint<float[]>, DataPoint<Float>> {

  public IntensityFilter(final DataProvider<DataPoint<float[]>> source) {
    source.addOnNewDatumListener(this);
  }

  @Override
  public void tell(final DataPoint<float[]> eventData) {

    float intensity = 0;
    for (float value : eventData.getValue()) {
      intensity += value;
    }
    intensity /= (float)eventData.getValue().length;

    final long timestamp = System.nanoTime();
    provideDatum(new DataPoint<>(timestamp, intensity));
  }
}
