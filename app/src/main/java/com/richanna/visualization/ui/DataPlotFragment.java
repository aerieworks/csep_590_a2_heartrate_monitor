package com.richanna.visualization.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.ui.TextOrientationType;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.richanna.events.Listener;
import com.richanna.heartratemonitor.R;
import com.richanna.visualization.DataSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link DataPlotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataPlotFragment extends Fragment implements Listener<DataSeries> {
  private final List<DataSeries> dataSerieses = new ArrayList<>();

  private XYPlot plot;

  public static DataPlotFragment newInstance(final DataSeries ... serieses) {
    DataPlotFragment fragment = new DataPlotFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    for (final DataSeries series : serieses) {
      fragment.addSeries(series);
    }
    return fragment;
  }

  public DataPlotFragment() {
    // Required empty public constructor
  }

  public void addSeries(final DataSeries series) {
    dataSerieses.add(series);
    series.onSeriesUpdated.listen(this);

    if (plot != null) {
      addSeriesToPlot(series);
      plot.redraw();
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    final View view = inflater.inflate(R.layout.fragment_data_plot, container, false);

    plot = (XYPlot) view.findViewById(R.id.dataPlot);
    plot.centerOnRangeOrigin(0);

    for (final DataSeries series : dataSerieses) {
      addSeriesToPlot(series);
    }

    plot.setTicksPerRangeLabel(3);
    plot.getGraphWidget().setDomainLabelPaint(null);
    plot.getGraphWidget().setDomainOriginLabelPaint(null);
    plot.getLayoutManager().remove(plot.getLegendWidget());
    plot.getLayoutManager().remove(plot.getTitleWidget());
    plot.getLayoutManager().remove(plot.getDomainLabelWidget());
    plot.getRangeLabelWidget().setOrientation(TextOrientationType.HORIZONTAL);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    if (plot != null) {
      plot.redraw();
    }
  }

  @Override
  public void tell(final DataSeries series) {
    if (plot != null) {
      plot.redraw();
    }
  }

  private void addSeriesToPlot(final DataSeries series) {
    final LineAndPointFormatter formatter = new LineAndPointFormatter();
    formatter.configure(getActivity().getApplicationContext(), series.getFormatterId());
    plot.addSeries(series, formatter);
  }
}
