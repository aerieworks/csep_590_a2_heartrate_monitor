package com.richanna.data.filters;

import android.util.Log;

import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;
import com.richanna.data.DataProviderBase;
import com.richanna.events.Listener;

import java.util.ArrayList;
import java.util.List;

public class FirFilter extends DataProviderBase<DataPoint<Float>> implements Listener<DataPoint<Float>> {

  private static final String TAG = "FirFilter";

  private final float[] coefficients;
  private final List<DataPoint<Float>> contextValues;

  public FirFilter(final float[] coefficients, final DataProvider<DataPoint<Float>> source) {
    this.coefficients = coefficients.clone();
    for (int i = 0; i < coefficients.length; i++) {
      this.coefficients[i] = coefficients[coefficients.length - i - 1];
    }

    contextValues = new ArrayList<>(coefficients.length);
    source.addOnNewDatumListener(this);

    final StringBuilder message = new StringBuilder("Coefficients: [");
    for (final float coeff : this.coefficients) {
      message.append(" ").append(coeff);
    }
    message.append(" ]");
    Log.i(TAG, message.toString());
  }

  @Override
  public void tell(DataPoint<Float> eventData) {
    if (contextValues.size() == coefficients.length) {
      contextValues.remove(0);
    }
    contextValues.add(eventData);

    if (contextValues.size() == coefficients.length) {
      float filteredValue = 0;
      for (int i = 0; i < contextValues.size(); i++) {
        final float x = contextValues.get(contextValues.size() - i - 1).getValue();
        final float coeff = coefficients[i];
        filteredValue += x * coeff;
      }

      provideDatum(new DataPoint<>(eventData.getTimestamp(), filteredValue));
    }
  }
}
