package com.richanna.sensors;

import android.hardware.Sensor;

import com.richanna.heartratemonitor.R;

public enum SensorInfo {
  Accelerometer(Sensor.TYPE_ACCELEROMETER,
      R.string.sensor_name_accelerometer),
  Gyroscope(Sensor.TYPE_GYROSCOPE,
      R.string.sensor_name_gyroscope),
  Gravity(Sensor.TYPE_GRAVITY,
      R.string.sensor_name_gravity);

  private final int sensorType;
  private final int sensorNameId;

  SensorInfo(final int sensorType,
             final int sensorNameId) {
    this.sensorType = sensorType;
    this.sensorNameId = sensorNameId;
  }

  public int getSensorType() { return sensorType; }
  public int getSensorNameId() { return sensorNameId; }
}
