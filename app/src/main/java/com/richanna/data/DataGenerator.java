package com.richanna.data;

import com.richanna.events.Event;

public abstract class DataGenerator {

  protected Event<DataPoint> newDataPointEvent = new Event<>();
  public Event<DataPoint>.Listenable onNewDataPoint = newDataPointEvent.listenable;

  public abstract void pause();
  public abstract void resume();
}
