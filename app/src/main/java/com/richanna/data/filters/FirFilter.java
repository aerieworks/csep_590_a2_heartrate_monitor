package com.richanna.data.filters;

import com.richanna.data.DataPoint;
import com.richanna.data.DataProviderBase;
import com.richanna.events.Listener;

import java.util.ArrayList;
import java.util.List;

public class FirFilter extends DataProviderBase<DataPoint> implements Listener<DataPoint> {

  private final float[] coefficients;
  private final List<DataPoint> contextValues;

  public FirFilter(final float[] coefficients) {
    // Reverse the coefficients that firwin() generated, to make the application straightforward.
    this.coefficients = new float[coefficients.length];
    for (int i = 0; i < coefficients.length; i++) {
      this.coefficients[i] = coefficients[coefficients.length - i - 1];
    }

    contextValues = new ArrayList<>(coefficients.length);
  }

  @Override
  public void tell(DataPoint eventData) {
    if (contextValues.size() == coefficients.length) {
      contextValues.remove(0);
    }
    contextValues.add(eventData);

    float filteredValue = 0;
    for (int i = 0; i < contextValues.size(); i++) {
      filteredValue += contextValues.get(i).getValues()[0] * coefficients[i];
    }

    provideDatum(new DataPoint(eventData.getTimestamp(), new float[] { filteredValue }));
  }
}
