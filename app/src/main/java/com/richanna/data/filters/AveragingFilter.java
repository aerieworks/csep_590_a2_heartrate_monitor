package com.richanna.data.filters;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;
import com.richanna.data.DataProviderBase;

public class AveragingFilter extends DataProviderBase<DataPoint<Float>> implements DataFilter<DataPoint<float[]>, DataPoint<Float>> {

  public AveragingFilter(final DataProvider<DataPoint<float[]>> source) {
    source.addOnNewDatumListener(this);
  }

  @Override
  public void tell(final DataPoint<float[]> eventData) {

    float total = 0;
    for (float value : eventData.getValue()) {
      total += value;
    }

    final float average = total / (float)eventData.getValue().length;
    provideDatum(new DataPoint<>(System.nanoTime(), average));
  }
}
