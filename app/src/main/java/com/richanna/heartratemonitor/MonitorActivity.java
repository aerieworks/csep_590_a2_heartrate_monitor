package com.richanna.heartratemonitor;

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
import com.richanna.data.filters.FftFilter;
import com.richanna.data.filters.IntensityFilter;
import com.richanna.data.filters.MeanShifter;
import com.richanna.data.visualization.DataSeries;
import com.richanna.data.visualization.StreamingSeries;
import com.richanna.data.visualization.WindowedSeries;
import com.richanna.events.Listener;
import com.richanna.sensors.CameraMonitor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MonitorActivity extends ActionBarActivity implements Listener<DataSeries> {

  private CameraMonitor cameraMonitor;
  private XYPlot rawPlot;
  private XYPlot fftPlot;

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
    cameraMonitor = new CameraMonitor(cameraView);

    rawPlot = initializePlot(R.id.rawDataPlot);
    fftPlot = initializePlot(R.id.fftPlot);

    final IntensityFilter intensityFilter = new IntensityFilter();
    cameraMonitor.addOnNewDatumListener(intensityFilter);
    //addSeriesToPlot(rawPlot, new StreamingSeries(intensityFilter, "Intensity", 0, R.xml.line_point_formatter_acceleration_x, 128));

    final MeanShifter demeanedIntensity = new MeanShifter(15);
    intensityFilter.addOnNewDatumListener(demeanedIntensity);
    addSeriesToPlot(rawPlot, new StreamingSeries(demeanedIntensity, "Demeaned", 0, R.xml.line_point_formatter_acceleration_y, 128));

    final FftFilter intensityFft = new FftFilter(32);
    demeanedIntensity.addOnNewDatumListener(intensityFft);
    addSeriesToPlot(fftPlot, new WindowedSeries(intensityFft, "FFT", R.xml.line_point_formatter_acceleration_z, 128));
  }

  @Override
  public void onResume() {
    super.onResume();
    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, openCvLoadListener);
    if (cameraMonitor != null) {
      cameraMonitor.resume();
    }
    if (rawPlot != null) {
      rawPlot.redraw();
    }
    if (fftPlot != null) {
      fftPlot.redraw();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (cameraMonitor != null) {
      cameraMonitor.pause();
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
    if (rawPlot != null) {
      rawPlot.redraw();
    }
    if (fftPlot != null) {
      fftPlot.redraw();
    }
  }

  private XYPlot initializePlot(final int plotId) {
    final XYPlot plot = (XYPlot) findViewById(plotId);
    plot.centerOnRangeOrigin(0);
    plot.setTicksPerRangeLabel(3);
    plot.getGraphWidget().setDomainLabelPaint(null);
    plot.getGraphWidget().setDomainOriginLabelPaint(null);
    plot.getLayoutManager().remove(plot.getLegendWidget());
    plot.getLayoutManager().remove(plot.getTitleWidget());
    plot.getLayoutManager().remove(plot.getDomainLabelWidget());
    plot.getRangeLabelWidget().setOrientation(TextOrientationType.HORIZONTAL);
    return plot;
  }

  private void addSeriesToPlot(final XYPlot plot, final DataSeries series) {
    series.onSeriesUpdated.listen(this);

    final LineAndPointFormatter formatter = new LineAndPointFormatter();
    formatter.configure(this, series.getFormatterId());
    plot.addSeries(series, formatter);
  }
}
