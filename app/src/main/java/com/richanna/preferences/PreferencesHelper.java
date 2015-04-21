package com.richanna.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferencesHelper {

  private static final String TAG = "PreferencesHelper";

  private final SharedPreferences preferences;
  private final Context context;

  public PreferencesHelper(final Context context) {
    preferences = PreferenceManager.getDefaultSharedPreferences(context);
    this.context = context;
  }

  public int getInteger(final int keyId, final int defaultValueId) {
    final String key = context.getString(keyId);
    final int defaultValue = Integer.parseInt(context.getString(defaultValueId));
    try {
      final String value = preferences.getString(key, null);
      if (value == null) {
        return defaultValue;
      } else {
        return Integer.parseInt(value);
      }
    } catch (ClassCastException ex) {
      Log.w(TAG, String.format("Could not read preference %s as integer, using default of %d", key, defaultValue));
      return defaultValue;
    }
  }

  public <T extends Enum> T getEnum(Class<T> enumClass, final int keyId, final int defaultValueId) {
    final int value = getInteger(keyId, defaultValueId);
    final T[] values = enumClass.getEnumConstants();
    return values[value];
  }
}
