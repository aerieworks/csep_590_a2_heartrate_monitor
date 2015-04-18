package com.richanna.data.visualization;

import android.util.Pair;

import com.androidplot.xy.XYSeries;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;
import com.richanna.events.Event;
import com.richanna.events.Listener;

public abstract class DataSeries implements Listener<DataPoint>, XYSeries {

  private final String title;
  private final DomainSource domainSource;
  private final int formatterId;
  protected final Event<DataSeries> seriesUpdatedEvent = new Event<>();
  public final Event<DataSeries>.Listenable onSeriesUpdated = seriesUpdatedEvent.listenable;

  protected DataSeries(final DataProvider<DataPoint> source, final String title, final DomainSource domainSource, final int formatterId) {
    this.title = title;
    this.domainSource = domainSource;
    this.formatterId = formatterId;
    source.addOnNewDatumListener(this);
  }

  protected abstract Pair<Number, Number> getDataPoint(final int index);
  protected abstract int getSize();

  public int getFormatterId() { return formatterId; }

  @Override
  public Number getX(int index) {
    if (getSize() > index) {
      if (domainSource == DomainSource.Index) {
        return index;
      } else {
        return getDataPoint(index).first;
      }
    }

    return index == 0 ? 0 : null;
  }

  @Override
  public Number getY(int index) {
    if (getSize() > index) {
      return getDataPoint(index).second;
    }

    return index == 0 ? 0 : null;
  }

  @Override
  public int size() {
    final int size = getSize();
    return size > 0 ? size : 1;
  }

  @Override
  public String getTitle() {
    return title;
  }

  public static enum DomainSource {
    Value,
    Index
  }
}
