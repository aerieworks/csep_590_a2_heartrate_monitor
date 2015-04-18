package com.richanna.data.visualization;

import android.util.Pair;

import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;

import java.util.ArrayList;
import java.util.List;

public class WindowedSeries extends DataSeries {

  private final int windowSize;
  private List<Pair<Number, Number>> currentWindow;
  private List<Pair<Number, Number>> nextWindow;

  public WindowedSeries(final DataProvider<DataPoint> source, final String title, final int formatterId, final int windowSize) {
    super(source, title, DomainSource.Index, formatterId);
    this.windowSize = windowSize;
    this.currentWindow = new ArrayList<>(windowSize);
    this.nextWindow = new ArrayList<>(windowSize);
  }

  @Override
  public void tell(final DataPoint dataPoint) {
    nextWindow.add(new Pair<Number, Number>(nextWindow.size(), dataPoint.getValues()[0]));
    if (nextWindow.size() == windowSize) {
      final List<Pair<Number, Number>> temp = currentWindow;
      currentWindow = nextWindow;
      nextWindow = temp;
      nextWindow.clear();
      seriesUpdatedEvent.fire(this);
    }
  }

  @Override
  protected int getSize() {
    return currentWindow.size();
  }

  @Override
  protected Pair<Number, Number> getDataPoint(int index) {
    return currentWindow.get(index);
  }
}
