package org.vmallocator;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Arrays;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartApp extends ApplicationFrame {
    public ChartApp(String title, VMType vm) {
        super(title);
        var chartPanel = (ChartPanel) createDemoPanel(vm);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }

    private JPanel createDemoPanel(VMType vm) {
        var chart = createChart(createDataset(vm), vm);
        var panel = new ChartPanel(chart, false);
        // panel.setFillZoomRectangle(true);
        // panel.setMouseWheelEnabled(true);
        return panel;
    }

    private JFreeChart createChart(XYDataset dataset, VMType vm) {
        var chart = ChartFactory.createXYLineChart(
                "Optimal Cluster", // title
                "VM Size", // x-axis label
                "Billable CPUs", // y-axis label
                dataset);

        chart.setBackgroundPaint(Color.WHITE);

        var plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        Arrays.sort(vm.sizes());
        var domainAxis = plot.getDomainAxis();
        domainAxis.setRange(vm.sizes()[0], vm.sizes()[vm.sizes().length - 1]);
        var domainUnits = new TickUnits();
        for (short size : vm.sizes()) {
            domainUnits.add(new NumberTickUnit(size));
        }
        domainAxis.setStandardTickUnits(domainUnits);
        domainAxis.setAutoRange(false);

        var rangeAxis = plot.getRangeAxis();
        rangeAxis.setAutoRange(true);

        if (plot.getRenderer() instanceof XYLineAndShapeRenderer r) {
            r.setDefaultShapesVisible(true);
            r.setDefaultShapesFilled(true);
            r.setDrawSeriesLineAsPath(true);
        }

        return chart;
    }

    private XYDataset createDataset(VMType vm) {
        var vmAllocator = new VMAllocator.Builder()
                .cpuPerProcess(2)
                .numberOfProcesses(64)
                .minimumVMCount(1)
                .cpuOverheadPerVm(1)
                .build();

        var allocations = vmAllocator.allocate(vm);

        var series = new XYSeries(vm.name());
        allocations.stream().forEach(info -> {
            series.add(info.vmSize(), info.totalBillableCPUs());
        });

        var dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }

    public static void main(String[] args) {
        var das5 = new VMType("Das v5", new BigDecimal("0.043"), new short[] { 2, 4, 8, 16, 32, 48, 64, 96 });
        var demo = new ChartApp("VM Allocator Chart", das5);
        demo.pack();
        UIUtils.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }
}
