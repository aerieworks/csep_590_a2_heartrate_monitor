package com.richanna.data.filters;

import android.util.Log;

import com.richanna.data.DataPoint;
import com.richanna.data.DataProviderBase;
import com.richanna.events.Listener;

import java.util.ArrayList;
import java.util.List;

public class FirFilter extends DataProviderBase<DataPoint> implements Listener<DataPoint> {

  private static final String TAG = "FirFilter";

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

    final StringBuilder buffer = new StringBuilder();
    float filteredValue = 0;
    for (int i = 0; i < contextValues.size(); i++) {
      final float x = contextValues.get(contextValues.size() - i - 1).getValues()[0];
      final float coeff = coefficients[i];
      filteredValue += x * coeff;

      if (i > 0) {
        buffer.append(" + ");
      }
      buffer.append(x).append("*").append(coeff);
    }

    buffer.append(" = ").append(filteredValue);
    Log.d(TAG, buffer.toString());
    provideDatum(new DataPoint(eventData.getTimestamp(), new float[] { filteredValue }));
  }
}
