package com.richanna.data;

import com.richanna.events.Event;
import com.richanna.events.Listener;

public class DataProviderBase<T> implements DataProvider<T> {

  private Event<T> newDatumEvent = new Event<>();

  protected void provideDatum(final T datum) {
    newDatumEvent.fire(datum);
  }

  @Override
  public void addOnNewDatumListener(final Listener<T> listener) {
    newDatumEvent.listenable.listen(listener);
  }
}
