package com.richanna.data;

import android.util.Log;

import com.richanna.events.Event;
import com.richanna.events.Listener;

public class DataProviderBase<T> implements DataProvider<T> {

  private Event<T> newDatumEvent = new Event<>();

  protected void provideDatum(final T datum) {
    //Log.d(this.getClass().getSimpleName(), String.format("Value: %s", datum.toString()));
    newDatumEvent.fire(datum);
  }

  @Override
  public void addOnNewDatumListener(final Listener<T> listener) {
    newDatumEvent.listenable.listen(listener);
  }

  @Override
  public void removeOnNewDatumListener(Listener<T> listener) {
    newDatumEvent.listenable.ignore(listener);
  }
}
