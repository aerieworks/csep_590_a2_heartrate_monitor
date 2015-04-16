package com.richanna.heartratemonitor;


import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.richanna.data.DataPoint;
import com.richanna.data.DataStream;
import com.richanna.data.filters.VectorFilter;
import com.richanna.sensors.CameraMonitor;
import com.richanna.sensors.SensorInfo;
import com.richanna.sensors.SensorMonitor;
import com.richanna.data.visualization.DataSeries;
import com.richanna.data.visualization.StreamingSeries;
import com.richanna.data.visualization.ui.DataPlotFragment;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


public class MonitorFragment extends Fragment {

  private SensorMonitor accelerationMonitor;

  public static MonitorFragment newInstance() {
    final MonitorFragment fragment = new MonitorFragment();
    fragment.setArguments(new Bundle());
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_monitor, container, false);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (accelerationMonitor == null) {
      initializePlot();
    }
    accelerationMonitor.resume();
  }

  @Override
  public void onPause() {
    super.onPause();
    accelerationMonitor.pause();
  }

  private void initializePlot() {
    final SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    accelerationMonitor = new SensorMonitor(sensorManager, SensorInfo.Accelerometer.getSensorType());
    accelerationMonitor.resume();

    final DataStream<DataPoint> xAccelStream = new DataStream(accelerationMonitor);
    xAccelStream.addFilter(new VectorFilter(0));
    final DataStream<DataPoint> yAccelStream = new DataStream(accelerationMonitor);
    yAccelStream.addFilter(new VectorFilter(1));
    final DataStream<DataPoint> zAccelStream = new DataStream(accelerationMonitor);
    zAccelStream.addFilter(new VectorFilter(2));

    final DataPlotFragment accelerationPlot = (DataPlotFragment) getChildFragmentManager().findFragmentById(R.id.accelerationPlot);
    accelerationPlot.addSeries(new StreamingSeries(xAccelStream, "X", DataSeries.DomainSource.Index, R.xml.line_point_formatter_acceleration_x, 300));
    accelerationPlot.addSeries(new StreamingSeries(yAccelStream, "Y", DataSeries.DomainSource.Index, R.xml.line_point_formatter_acceleration_y, 300));
    accelerationPlot.addSeries(new StreamingSeries(zAccelStream, "Z", DataSeries.DomainSource.Index, R.xml.line_point_formatter_acceleration_z, 300));
  }
}
