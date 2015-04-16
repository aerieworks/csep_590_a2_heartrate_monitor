package com.richanna.data;

import com.richanna.events.Listener;

import java.util.ArrayList;
import java.util.List;

public class DataStream<T> extends DataGenerator<T> {

  private final DataGenerator<T> source;
  private final List<DataFilter<T>> filterChain = new ArrayList<>();

  public DataStream(final DataGenerator<T> source) {
    this.source = source;
    source.onNewDatum.listen(new Listener<T>() {
      @Override
      public void tell(T datum) {
        for (final DataFilter<T> filter : filterChain) {
          datum = filter.apply(datum);
          if (datum == null) {
            return;
          }
        }

        newDatumEvent.fire(datum);
      }
    });
  }

  public DataStream<T> addFilter(final DataFilter<T> filter) {
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

  public DataStream<T> removeFilter(final DataFilter<T> filter) {
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
