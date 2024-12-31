package org.example.GA.Charts;


import org.example.GA.Evolution.Evolution;
import org.example.PSO.Flock;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;



public class MakespanChartExample {

    // Method to create the dataset from makespansOfSomeProjects
    public static XYSeriesCollection createDataset(Evolution.Run evoRun, Flock.Run flockRun) {


        XYSeriesCollection dataset = new XYSeriesCollection();

        for (Map.Entry<String, ArrayList<Integer>> entry : evoRun.makeSpansOfSomeProjects.entrySet()) {
            String projectName = entry.getKey();
            ArrayList<Integer> makespans = entry.getValue();

            XYSeries series = new XYSeries(projectName);

            // Add data points to the series: x is the index, y is the makespan value
            for (int i = 0; i < makespans.size(); i++) {
                series.add(i, makespans.get(i));
            }

            dataset.addSeries(series);
        }

        XYSeries seriesGBest = new XYSeries("PSO gBest");
        for (int i = 0; i< flockRun.gBestInIterations.length; i++)
            seriesGBest.add(i, flockRun.gBestInIterations[i]);
        dataset.addSeries(seriesGBest);





        return dataset;
    }

    // Method to create the chart
    public static JFreeChart createChart(XYSeriesCollection dataset, String title) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, // Chart title
                "iteration",             // X-axis label
                "Makespan",          // Y-axis label
                dataset,             // Data
                PlotOrientation.VERTICAL,
                true,                // Include legend
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        plot.setRenderer(renderer);
        return chart;
    }

    // Method to save the chart as an image
    public static void saveChartAsImage(JFreeChart chart, String filePath) {
        File imageFile = new File(filePath);
        int width = 800;  // Set width of image
        int height = 600; // Set height of image
        try {
            ChartUtils.saveChartAsPNG(imageFile, chart, width, height);
            System.out.println("Chart saved as image to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving chart as image: " + e.getMessage());
        }
    }


}