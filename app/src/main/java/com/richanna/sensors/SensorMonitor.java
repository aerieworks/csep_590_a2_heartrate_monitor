package com.richanna.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.richanna.data.DataGenerator;
import com.richanna.data.DataPoint;

public class SensorMonitor extends DataGenerator implements SensorEventListener {

  private final SensorManager sensorManager;
  private final int sensorType;

  public SensorMonitor(final SensorManager sensorManager, final int sensorType) {
    this.sensorManager = sensorManager;
    this.sensorType = sensorType;
  }

  @Override
  public void pause() {
    sensorManager.unregisterListener(this);
  }

  @Override
  public void resume() {
    sensorManager.registerListener(this, sensorManager.getDefaultSensor(sensorType), SensorManager.SENSOR_DELAY_FASTEST);
  }

  @Override
  public void onSensorChanged(final SensorEvent event) {

    if (event.sensor.getType() == sensorType) {
      final DataPoint dataPoint = new DataPoint(event.timestamp, event.values);
      newDatumEvent.fire(dataPoint);
    }
  }

  @Override
  public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
    // Do nothing for this event.
  }
}
