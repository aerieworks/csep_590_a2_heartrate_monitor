package com.richanna.heartratemonitor;

import android.content.Context;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.androidplot.ui.TextOrientationType;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.richanna.data.DataPoint;
import com.richanna.data.DataStream;
import com.richanna.data.filters.VectorFilter;
import com.richanna.data.visualization.DataSeries;
import com.richanna.data.visualization.StreamingSeries;
import com.richanna.events.Listener;
import com.richanna.sensors.CameraMonitor;
import com.richanna.sensors.SensorInfo;
import com.richanna.sensors.SensorMonitor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MonitorActivity extends ActionBarActivity implements Listener<DataSeries> {

  private CameraMonitor cameraMonitor;
  private SensorMonitor accelerationMonitor;
  private XYPlot plot;

  private final BaseLoaderCallback openCvLoadListener = new BaseLoaderCallback(this) {
    @Override
    public void onManagerConnected(int status) {
      switch (status) {
        case LoaderCallbackInterface.SUCCESS:
          Log.i("OpenCV", "OpenCV loaded successfully");
          if (cameraMonitor != null) {
            cameraMonitor.resume();
          }
          break;
        default:
          super.onManagerConnected(status);
          break;
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_monitor);

    OpenCVLoader.initDebug();

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    final CameraView cameraView = (CameraView) findViewById(R.id.cameraView);
    cameraView.setVisibility(SurfaceView.VISIBLE);
    cameraView.enableView();
    cameraMonitor = new CameraMonitor(cameraView);

    plot = (XYPlot) findViewById(R.id.rawDataPlot);
    final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    accelerationMonitor = new SensorMonitor(sensorManager, SensorInfo.Accelerometer.getSensorType());
    accelerationMonitor.resume();

    final DataStream<DataPoint> xAccelStream = new DataStream(accelerationMonitor);
    xAccelStream.addFilter(new VectorFilter(0));
    final DataStream<DataPoint> yAccelStream = new DataStream(accelerationMonitor);
    yAccelStream.addFilter(new VectorFilter(1));
    final DataStream<DataPoint> zAccelStream = new DataStream(accelerationMonitor);
    zAccelStream.addFilter(new VectorFilter(2));

    addSeriesToPlot(new StreamingSeries(xAccelStream, "X", DataSeries.DomainSource.Index, R.xml.line_point_formatter_acceleration_x, 300));
    addSeriesToPlot(new StreamingSeries(yAccelStream, "Y", DataSeries.DomainSource.Index, R.xml.line_point_formatter_acceleration_y, 300));
    addSeriesToPlot(new StreamingSeries(zAccelStream, "Z", DataSeries.DomainSource.Index, R.xml.line_point_formatter_acceleration_z, 300));

    plot.centerOnRangeOrigin(0);
    plot.setTicksPerRangeLabel(3);
    plot.getGraphWidget().setDomainLabelPaint(null);
    plot.getGraphWidget().setDomainOriginLabelPaint(null);
    plot.getLayoutManager().remove(plot.getLegendWidget());
    plot.getLayoutManager().remove(plot.getTitleWidget());
    plot.getLayoutManager().remove(plot.getDomainLabelWidget());
    plot.getRangeLabelWidget().setOrientation(TextOrientationType.HORIZONTAL);


  }

  @Override
  public void onResume() {
    super.onResume();
    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, openCvLoadListener);
    if (cameraMonitor != null) {
      cameraMonitor.resume();
    }
    if (accelerationMonitor != null) {
      accelerationMonitor.resume();
    }
    if (plot != null) {
      plot.redraw();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (cameraMonitor != null) {
      cameraMonitor.pause();
    }
    if (accelerationMonitor != null) {
      accelerationMonitor.pause();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_monitor, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void tell(final DataSeries series) {
    if (plot != null) {
      plot.redraw();
    }
  }

  private void addSeriesToPlot(final DataSeries series) {
    series.onSeriesUpdated.listen(this);

    final LineAndPointFormatter formatter = new LineAndPointFormatter();
    formatter.configure(this, series.getFormatterId());
    plot.addSeries(series, formatter);
  }
}
