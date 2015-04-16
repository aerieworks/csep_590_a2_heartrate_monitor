package com.richanna.heartratemonitor;


import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.richanna.data.DataStream;
import com.richanna.filters.VectorFilter;
import com.richanna.sensors.SensorInfo;
import com.richanna.sensors.SensorMonitor;
import com.richanna.visualization.DataSeries;
import com.richanna.visualization.StreamingSeries;
import com.richanna.visualization.ui.DataPlotFragment;


public class MonitorFragment extends Fragment {

  private SensorMonitor accelerationMonitor;
  private DataStream xAccelStream;
  private DataStream yAccelStream;
  private DataStream zAccelStream;

  public MonitorFragment() {
    // Required empty public constructor
  }

  public static MonitorFragment newInstance() {
    final MonitorFragment fragment = new MonitorFragment();
    fragment.setArguments(new Bundle());
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    accelerationMonitor = new SensorMonitor(sensorManager, SensorInfo.Accelerometer.getSensorType());
    accelerationMonitor.resume();

    xAccelStream = new DataStream(accelerationMonitor);
    xAccelStream.addFilter(new VectorFilter(0));
    yAccelStream = new DataStream(accelerationMonitor);
    yAccelStream.addFilter(new VectorFilter(1));
    zAccelStream = new DataStream(accelerationMonitor);
    zAccelStream.addFilter(new VectorFilter(2));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    final View view = inflater.inflate(R.layout.fragment_monitor, container, false);

    final DataPlotFragment accelerationPlot = (DataPlotFragment) getChildFragmentManager().findFragmentById(R.id.accelerationPlot);
    accelerationPlot.addSeries(new StreamingSeries(xAccelStream, "X", DataSeries.DomainSource.Index, R.xml.line_point_formatter_acceleration_x, 300));
    accelerationPlot.addSeries(new StreamingSeries(yAccelStream, "Y", DataSeries.DomainSource.Index, R.xml.line_point_formatter_acceleration_y, 300));
    accelerationPlot.addSeries(new StreamingSeries(zAccelStream, "Z", DataSeries.DomainSource.Index, R.xml.line_point_formatter_acceleration_z, 300));

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    accelerationMonitor.resume();
  }

  @Override
  public void onPause() {
    super.onPause();
    accelerationMonitor.pause();
  }
}
