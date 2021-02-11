package com.mushin.muconnect.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mushin.muconnect.R;
import com.mushin.muconnect.databinding.FragmentMushinBinding;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class MushinFragment extends Fragment {
    private PageViewModel pageViewModel;
    private FragmentMushinBinding binding;

    public static MushinFragment newInstance() {
        return new MushinFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(getActivity()).get(PageViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mushin, container, false);

        binding.setModel(pageViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        View root = binding.getRoot();

        // region crank graph
        GraphView cranksGraph = (GraphView) root.findViewById(R.id.cranksGraph);
        cranksGraph.setTitle("Crank data");
        cranksGraph.getLegendRenderer().setVisible(true);
        cranksGraph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
        cranksGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        cranksGraph.getViewport().setXAxisBoundsManual(true);
        cranksGraph.getViewport().setMinX(0);
        cranksGraph.getViewport().setMaxX(400);

        LineGraphSeries<DataPoint> leftCrankSeries = pageViewModel.getLeftCrankSeries();
        leftCrankSeries.setColor(Color.BLUE);
        leftCrankSeries.setTitle("Left Crank");
        cranksGraph.addSeries(leftCrankSeries);

        LineGraphSeries<DataPoint> rightCrankSeries = pageViewModel.getRightCrankSeries();
        rightCrankSeries.setColor(Color.RED);
        rightCrankSeries.setTitle("Right Crank");
        cranksGraph.addSeries(rightCrankSeries);

        // endregion

        // region acc graph
        GraphView accGraph = (GraphView) root.findViewById(R.id.accGraph);
        accGraph.setTitle("Accelerometer");
        accGraph.getLegendRenderer().setVisible(true);
        accGraph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
        accGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        accGraph.getViewport().setXAxisBoundsManual(true);
        accGraph.getViewport().setMinX(0);
        accGraph.getViewport().setMaxX(400);

        LineGraphSeries<DataPoint> xAccSeries = pageViewModel.getAccXSeries();
        xAccSeries.setColor(Color.RED);
        xAccSeries.setTitle("X Axis");
        accGraph.addSeries(xAccSeries);

        LineGraphSeries<DataPoint> yAccSeries = pageViewModel.getAccYSeries();
        yAccSeries.setColor(Color.GREEN);
        yAccSeries.setTitle("Y Axis");
        accGraph.addSeries(yAccSeries);

        LineGraphSeries<DataPoint> zAccSeries = pageViewModel.getAccZSeries();
        zAccSeries.setColor(Color.BLUE);
        zAccSeries.setTitle("Z Axis");
        accGraph.addSeries(zAccSeries);

        // endregion

        // region gyro graph
        GraphView gyroGraph = (GraphView) root.findViewById(R.id.gyroGraph);
        gyroGraph.setTitle("Gyroscope");
        gyroGraph.getLegendRenderer().setVisible(true);
        gyroGraph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
        gyroGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        gyroGraph.getViewport().setXAxisBoundsManual(true);
        gyroGraph.getViewport().setMinX(0);
        gyroGraph.getViewport().setMaxX(400);

        LineGraphSeries<DataPoint> xGyroSeries = pageViewModel.getGyroXSeries();
        xGyroSeries.setColor(Color.RED);
        xGyroSeries.setTitle("X Axis");
        gyroGraph.addSeries(xGyroSeries);

        LineGraphSeries<DataPoint> yGyroSeries = pageViewModel.getGyroYSeries();
        yGyroSeries.setColor(Color.GREEN);
        yGyroSeries.setTitle("Y Axis");
        gyroGraph.addSeries(yGyroSeries);

        LineGraphSeries<DataPoint> zGyroSeries = pageViewModel.getGyroZSeries();
        zGyroSeries.setColor(Color.BLUE);
        zGyroSeries.setTitle("Z Axis");
        gyroGraph.addSeries(zGyroSeries);

        // endregion

        return root;
    }

}
