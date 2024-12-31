package org.example.GA.Charts;

import org.example.GA.Evolution.Evolution;
import org.example.GA.Evolution.EvolutionUtils;
import org.example.GA.Individual.Individual;
import org.example.GA.Individual.Node.AttributesAndFunctions.AttributeMap;
import org.example.GA.Utils.IndividualUtils;
import org.example.PSO.Flock;
import org.example.Project.Project;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ChartUtilsHelper {








    public static void writeTreesToFiles(Evolution.Run evoRun, Project project, String pathToSave) {

        System.out.println("write trees");
        System.out.println(pathToSave);

        // Printing stats from run
        for (int i=0;i<evoRun.bestFitnessIndex.size();i++) {
            // printing fitnesses
            System.out.println(String.format("Avg: %.3f, Best: %.3f, Worst: %.3f", evoRun.avgFitnesses.get(i), evoRun.bestFitness.get(i), evoRun.worstFitness.get(i)));
            System.out.println("dev: " + (evoRun.bestFitness.get(i) - project.getLB()));

            // printing counts of picked rules
            if (i == evoRun.bestFitnessIndex.size() - 1) {
                System.out.println("");

                String fileNamePop = String.format("%s Attribute counts final pop.txt", project.getRef1());
                Map<AttributeMap, Integer> attributeCounts = EvolutionUtils.countAttributesInPopulation(evoRun.individualsFinalPop);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathToSave + fileNamePop))) {                 writer.write("Attribute counts for the final population:\n");
                    for (Map.Entry<AttributeMap, Integer> entry : attributeCounts.entrySet())
                        writer.write(String.format("Attribute: %s, Count: %d%n", entry.getKey(), entry.getValue()));
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                }





                String fileNameInd = String.format("%s Attribute counts best individual.txt", project.getRef1());
                Individual bestIndividual = evoRun.individualsFinalPop[evoRun.bestFitnessIndex.get(i)];
                Map<AttributeMap, Integer> attributeCountsBest = EvolutionUtils.countAttributesOfIndividual(bestIndividual);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathToSave + fileNameInd))) {
                    writer.write("Attribute counts for the best individual:\n");
                    writer.write("Best makespan: " + (bestIndividual.getFitness()) + ", deviation: " + (bestIndividual.getFitness() - project.getLB()) + "\n");
                    for (Map.Entry<AttributeMap, Integer> entry : attributeCountsBest.entrySet()) {
                        writer.write(String.format("Attribute: %s, Count: %d%n", entry.getKey(), entry.getValue()));
                    }
                    writer.write("\n");

                    // Find and write the height
                    int height = IndividualUtils.findHeight(bestIndividual.getRoot());
                    writer.write(String.format("Height: %d%n", height));

                    // Write the tree structure
                    writer.write("\nTree Structure:\n");
                    String tree = bestIndividual.printTree();
                    writer.write(tree);


                    System.out.println("Best individual details successfully saved to " + fileNameInd);
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                }
            }

        }
    }



    private static Float findTopRange(Evolution.Run evoRun, Flock.Run flockRun) {
        Float topValue = 0f;
        for (int i=0;i<evoRun.bestFitness.size();i++) {
            if (evoRun.getBestFitness().get(i) > topValue) topValue = evoRun.getBestFitness().get(i);
            else if (flockRun.gBestInIterations[i] > topValue) topValue = (float) flockRun.gBestInIterations[i];
        }
        return topValue;
    }

    private static Float findBottomRange(Evolution.Run evoRun, Flock.Run flockRun) {
        Float bottomValue = 100000f;
        for (int i=0;i<evoRun.bestFitness.size();i++) {
            if (evoRun.getBestFitness().get(i) < bottomValue) bottomValue = evoRun.getBestFitness().get(i);
            else if (flockRun.gBestInIterations[i] < bottomValue) bottomValue = (float) flockRun.gBestInIterations[i];

        }
        return bottomValue;
    }



    public static void chartMakespansOverTime(Evolution.Run evoRun, Flock.Run flockRun, String pathToSave, Project project) {


        XYSeriesCollection makespanmakespandatasetForChartForChart = MakespanChartExample.createDataset(evoRun, flockRun);
        JFreeChart chart = MakespanChartExample.createChart(makespanmakespandatasetForChartForChart, project.getRef1());

// Set the y-axis range
        ValueAxis yAxis = chart.getXYPlot().getRangeAxis();
        Float bottomValue = findBottomRange(evoRun, flockRun) - 2;
        Float topValue = findTopRange(evoRun, flockRun) + 2;



        yAxis.setRange(bottomValue, topValue); // Set the desired range for the y-axis

// Save the chart as an image
        String filename = String.format("%s Makespans.png", project.getRef1());
        String fullPath = pathToSave + filename;
        MakespanChartExample.saveChartAsImage(chart, fullPath);






    }


    public static void makeHistogramOfWins(Map<String, Integer> winsInDatasetsEvo,
                                           Map<String, Integer> winsInDatasetsPso,
                                           String pathToSave, String filename) {

        // Create the dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Populate the dataset from winsInDatasetsEvo
        for (Map.Entry<String, Integer> entry : winsInDatasetsEvo.entrySet()) {
            dataset.addValue(entry.getValue(), "Evo", entry.getKey());
        }

        // Populate the dataset from winsInDatasetsPso
        for (Map.Entry<String, Integer> entry : winsInDatasetsPso.entrySet()) {
            dataset.addValue(entry.getValue(), "Pso", entry.getKey());
        }

        // Create the bar chart
        JFreeChart barChart = ChartFactory.createBarChart(
                "Wins in Datasets", // Chart title
                "Dataset",          // X-axis Label
                "Wins",             // Y-axis Label
                dataset,            // Data
                PlotOrientation.VERTICAL, // Orientation
                true,               // Include legend
                true,               // Tooltips
                false               // URLs
        );

        // Save the chart as a PNG file
        try {
            File file = new File(pathToSave + filename); // Specify your desired location
            ChartUtils.saveChartAsPNG(file, barChart, 800, 600); // Width: 800px, Height: 600px
            System.out.println("Histogram saved at: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving chart: " + e.getMessage());
        }



    }


}
