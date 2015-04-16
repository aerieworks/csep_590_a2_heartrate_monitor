package com.richanna.visualization;

import android.util.Pair;

import com.androidplot.xy.XYSeries;
import com.richanna.data.DataGenerator;
import com.richanna.data.DataSink;
import com.richanna.events.Event;

public abstract class DataSeries implements DataSink, XYSeries {

  private final String title;
  private final DomainSource domainSource;
  private final int formatterId;
  protected final Event<DataSeries> seriesUpdatedEvent = new Event<>();
  public final Event<DataSeries>.Listenable onSeriesUpdated = seriesUpdatedEvent.listenable;

  protected DataSeries(final DataGenerator source, final String title, final DomainSource domainSource, final int formatterId) {
    this.title = title;
    this.domainSource = domainSource;
    this.formatterId = formatterId;
    source.onNewDataPoint.listen(this);
  }

  protected abstract Pair<Number, Number> getDataPoint(final int index);

  public int getFormatterId() { return formatterId; }

  @Override
  public Number getX(int index) {
    if (size() > index) {
      if (domainSource == DomainSource.Index) {
        return index;
      } else {
        return getDataPoint(index).first;
      }
    }

    return null;
  }

  @Override
  public Number getY(int index) {
    if (size() > index) {
      return getDataPoint(index).second;
    }

    return null;
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
