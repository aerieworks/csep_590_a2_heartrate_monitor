package com.richanna.data;

import java.util.ArrayList;
import java.util.List;

public class DataStream extends DataGenerator {

  private final DataGenerator source;
  private final List<DataFilter> filterChain = new ArrayList<>();

  public DataStream(final DataGenerator source) {
    this.source = source;
    source.onNewDataPoint.listen(new DataSink() {
      @Override
      public void tell(DataPoint dataPoint) {
        for (final DataFilter filter : filterChain) {
          dataPoint = filter.apply(dataPoint);
          if (dataPoint == null) {
            return;
          }
        }

        newDataPointEvent.fire(dataPoint);
      }
    });
  }

  public DataStream addFilter(final DataFilter filter) {
    if (!filterChain.contains(filter)) {
      filterChain.add(filter);
    }

    return this;
  }

  public DataStream addFilter(final int location, final DataFilter filter) {
    if (!filterChain.contains(filter)) {
      filterChain.add(location, filter);
    }

    return this;
  }

  public DataStream removeFilter(final DataFilter filter) {
    filterChain.remove(filter);
    return this;
  }

  @Override
  public void pause() {
    source.pause();
  }

  @Override
  public void resume() {
    source.resume();
  }
}
