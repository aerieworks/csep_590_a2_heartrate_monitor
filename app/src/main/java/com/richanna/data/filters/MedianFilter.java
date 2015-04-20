package com.richanna.data.filters;

import com.richanna.data.DataFilter;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;
import com.richanna.data.DataProviderBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianFilter extends DataProviderBase<DataPoint<Float>> implements DataFilter<DataPoint<Float>, DataPoint<Float>> {

  private final int windowSize;
  private final List<Float> window = new ArrayList<>();

  public MedianFilter(final int windowSize, final DataProvider<DataPoint<Float>> source) {
    this.windowSize = windowSize;
    source.addOnNewDatumListener(this);
  }

  @Override
  public void tell(DataPoint<Float> dataPoint) {
    window.add(dataPoint.getValue());
    while (window.size() > windowSize) {
      window.remove(0);
    }

    if (window.size() == windowSize) {
      final List<Float> values = new ArrayList<>(window);
      Collections.sort(values);

      final float medianValue = values.get(values.size() / 2);
      provideDatum(new DataPoint<>(dataPoint.getTimestamp(), medianValue));
    }
  }
}
