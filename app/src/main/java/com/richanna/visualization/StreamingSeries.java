package com.richanna.visualization;

import android.util.Pair;

import com.richanna.data.DataGenerator;
import com.richanna.data.DataPoint;

import java.util.ArrayList;
import java.util.List;

public class StreamingSeries extends DataSeries {

  private final int maxSize;
  private final List<Pair<Number, Number>> series;

  public StreamingSeries(final DataGenerator source, final String title, final DomainSource domainSource, final int formatterId, final int maxSize) {
    super(source, title, domainSource, formatterId);
    this.maxSize = maxSize;
    series = new ArrayList<>(maxSize);
  }

  @Override
  public void tell(final DataPoint dataPoint) {
    if (series.size() == maxSize) {
      series.remove(0);
    }

    series.add(new Pair<Number, Number>(dataPoint.getValues()[0], dataPoint.getValues()[1]));
    seriesUpdatedEvent.fire(this);
  }

  @Override
  public int size() {
    return series.size();
  }

  @Override
  protected Pair<Number, Number> getDataPoint(int index) {
    return series.get(index);
  }
}
