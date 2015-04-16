package com.richanna.data;

public interface DataFilter<T> {
  public T apply(final T datum);
}
