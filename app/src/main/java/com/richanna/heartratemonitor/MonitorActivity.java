package com.richanna.heartratemonitor;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidplot.ui.TextOrientationType;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.richanna.data.DataPoint;
import com.richanna.data.DataProvider;
import com.richanna.data.DataWindow;
import com.richanna.data.filters.AveragingFilter;
import com.richanna.data.filters.FftFilter;
import com.richanna.data.filters.FirFilter;
import com.richanna.data.filters.DemeanFilter;
import com.richanna.data.filters.MedianFilter;
import com.richanna.data.filters.PixelSampler;
import com.richanna.data.filters.VectorFilter;
import com.richanna.data.filters.VectorRangeFilter;
import com.richanna.data.visualization.DataSeries;
import com.richanna.data.visualization.StreamingSeries;
import com.richanna.data.visualization.WindowedSeries;
import com.richanna.events.Listener;
import com.richanna.preferences.PreferencesHelper;
import com.richanna.sensors.CameraMonitor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class MonitorActivity extends ActionBarActivity implements PreferenceChangeListener {

  private static final float[] FIR_COEFF_25FPS_6TAP = new float[] { 0.00809077f, 0.11386596f, 0.37804327f, 0.37804327f, 0.11386596f, 0.00809077f };

  private static final int FFT_WINDOW_SIZE = 256;
  private static final float MAX_HEART_RATE_HZ = 4.0f;

  private PreferencesHelper preferencesHelper;
  private CameraMonitor cameraMonitor;
  private XYPlot signalPlot;
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
    preferencesHelper = new PreferencesHelper(this);

    signalPlot = initializePlot(R.id.signalPlot);
    fftPlot = initializePlot(R.id.fftPlot);
    resetMonitors();
  }

  @Override
  public void onResume() {
    super.onResume();
    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, openCvLoadListener);
    if (cameraMonitor != null) {
      cameraMonitor.resume();
    }
    if (signalPlot != null) {
      signalPlot.redraw();
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
    final int id = item.getItemId();
    if (id == R.id.action_reset) {
      resetMonitors();
      return true;
    } else if (id == R.id.action_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void preferenceChange(PreferenceChangeEvent preferenceChangeEvent) {
    resetMonitors();
  }

  private void resetMonitors() {
    cameraMonitor.clearListeners();
    signalPlot.clear();
    fftPlot.clear();
    fftPlot.redraw();

    final int medianWindow = preferencesHelper.getInteger(R.string.pref_key_median_filter_window_size, R.string.pref_default_median_filter_window_size);
    final int demeanWindow = preferencesHelper.getInteger(R.string.pref_key_demean_filter_window_size, R.string.pref_default_demean_filter_window_size);

    final DataProvider<DataPoint<Float>> stream =
        new DemeanFilter(demeanWindow,
            new MedianFilter(medianWindow,
                new FirFilter(FIR_COEFF_25FPS_6TAP,
                    createIntensityFilter(createPixelSampler())
                )
            )
        );
    addSeriesToPlot(signalPlot, new StreamingSeries(stream, "Intensity", R.xml.line_point_formatter_acceleration_x, FFT_WINDOW_SIZE));

    final FftFilter intensityFft = new FftFilter(FFT_WINDOW_SIZE, stream);
    addSeriesToPlot(fftPlot, new WindowedSeries(intensityFft, "FFT", R.xml.line_point_formatter_acceleration_x));

    initializeProgressBar(stream, intensityFft);
    initializeHeartbeatMonitor(intensityFft);
  }

  private DataProvider<DataPoint<float[]>> createPixelSampler() {
    final PixelSampler.ImageSampleMode sampleMode = preferencesHelper.getEnum(PixelSampler.ImageSampleMode.class, R.string.pref_key_image_sample_mode, R.string.pref_default_image_sample_mode);
    final int sampleSize = preferencesHelper.getInteger(R.string.pref_key_image_sample_size, R.string.pref_default_image_sample_size);
    return new PixelSampler(sampleMode, sampleSize, cameraMonitor);
  }

  private DataProvider<DataPoint<Float>> createIntensityFilter(final DataProvider<DataPoint<float[]>> source) {
    final IntensityCalculationMode calculationMode = preferencesHelper.getEnum(IntensityCalculationMode.class, R.string.pref_key_intensity_calculation_mode, R.string.pref_default_intensity_calculation_mode);
    if (calculationMode == IntensityCalculationMode.RgbAverage) {
      return new AveragingFilter(new VectorRangeFilter(0, 2, source));
    } else if (calculationMode == IntensityCalculationMode.RedChannelOnly) {
      return new VectorFilter(0, source);
    } else if (calculationMode == IntensityCalculationMode.Grayscale) {
      return new VectorFilter(4, source);
    }

    throw new IllegalArgumentException("Unrecognized intensity calculation mode: " + calculationMode.toString());
  }

  private void initializeProgressBar(final DataProvider<DataPoint<Float>> progressAdvancer, final FftFilter fftFilter) {
    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
    progressBar.setProgress(0);
    progressBar.setMax(FFT_WINDOW_SIZE);

    fftFilter.addOnNewDatumListener(new Listener<DataWindow<DataPoint<Float>>>() {
      @Override
      public void tell(DataWindow<DataPoint<Float>> eventData) {
        progressBar.setProgress(0);
        progressBar.setMax((int) (FFT_WINDOW_SIZE * 0.25));
        signalPlot.redraw();
        fftPlot.redraw();
      }
    });

    progressAdvancer.addOnNewDatumListener(new Listener<DataPoint<Float>>() {
      @Override
      public void tell(DataPoint<Float> eventData) {
        progressBar.incrementProgressBy(1);
      }
    });
  }

  private void initializeHeartbeatMonitor(final DataProvider<DataWindow<DataPoint<Float>>> provider) {
    final TextView heartRateView = (TextView) findViewById(R.id.heartRateView);
    heartRateView.setText(getString(R.string.default_heart_rate));

    provider.addOnNewDatumListener(new Listener<DataWindow<DataPoint<Float>>>() {
      @Override
      public void tell(DataWindow<DataPoint<Float>> eventData) {
        final float timespan = (float) (eventData.getEndTime() - eventData.getStartTime()) / 1000000000f;
        final float sampleRate = (float) FFT_WINDOW_SIZE / timespan;
        final float rateStep = (sampleRate / 2.0f) / (float) eventData.getSize();

        Log.d("MonitorActivity", String.format("Sample rate: %f", sampleRate));
        int maxIndex = 0;
        float maxValue = 0;
        int index = 0;
        for (final DataPoint<Float> dataPoint : eventData.getData()) {
          if (index * rateStep > MAX_HEART_RATE_HZ) {
            break;
          }

          if (dataPoint.getValue() > maxValue) {
            maxIndex = index;
            maxValue = dataPoint.getValue();
          }

          index += 1;
        }

        final float frequency = (float) maxIndex * rateStep;
        final int bpm = (int) (frequency * 60.0f);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            heartRateView.setText(Integer.toString(bpm));
          }
        });
      }
    });
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
    series.onSeriesUpdated.listen(new Listener<DataSeries>() {
      @Override
      public void tell(DataSeries eventData) {
        //plot.redraw();
      }
    });

    final LineAndPointFormatter formatter = new LineAndPointFormatter();
    formatter.configure(this, series.getFormatterId());
    plot.addSeries(series, formatter);
  }

  public static enum IntensityCalculationMode {
    RgbAverage,
    RedChannelOnly,
    Grayscale
  }
}
