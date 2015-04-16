package com.richanna.data;

import com.richanna.events.Event;

public abstract class DataGenerator<T> {

  protected Event<T> newDatumEvent = new Event<>();
  public Event<T>.Listenable onNewDatum = newDatumEvent.listenable;

  public abstract void pause();
  public abstract void resume();
}
