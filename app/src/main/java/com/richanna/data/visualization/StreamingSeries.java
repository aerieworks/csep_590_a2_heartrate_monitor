package com.richanna.data.visualization;

import android.util.Pair;

import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;

import java.util.ArrayList;
import java.util.List;

public class StreamingSeries extends DataSeries {

  private final int maxSize;
  private final List<Pair<Number, Number>> series;
  private final int rangeIndex;
  private int index = 0;

  public StreamingSeries(final DataProvider<DataPoint> source, final String title, final int rangeIndex, final int formatterId, final int maxSize) {
    super(source, title, DomainSource.Index, formatterId);
    this.maxSize = maxSize;
    series = new ArrayList<>(maxSize);
    this.rangeIndex = rangeIndex;
  }

  @Override
  public void tell(final DataPoint dataPoint) {
    if (series.size() == maxSize) {
      series.remove(0);
    }

    series.add(new Pair<Number, Number>(index, dataPoint.getValues()[rangeIndex]));
    index += 1;
    seriesUpdatedEvent.fire(this);
  }

  @Override
  protected int getSize() {
    return series.size();
  }

  @Override
  protected Pair<Number, Number> getDataPoint(int index) {
    return series.get(index);
  }
}
