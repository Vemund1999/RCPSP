package org.example;

import lombok.Data;
import org.example.GA.Charts.ChartUtilsHelper;
import org.example.GA.Evolution.Evolution;
import org.example.GA.Individual.Node.FunctionNode;
import org.example.GA.Individual.Node.Node;
import org.example.PSO.Flock;
import org.example.Project.Project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {











    public static void runProjectTest() throws IOException, InterruptedException {




        // example of some projects
        List<String> projectPathsToShowScheduleJ30 = new ArrayList<>();
        projectPathsToShowScheduleJ30.add("j3011_10.sm");
        projectPathsToShowScheduleJ30.add("j3018_7.sm");
        projectPathsToShowScheduleJ30.add("j3025_2.sm");
        projectPathsToShowScheduleJ30.add("j3038_5.sm"); // 3
        projectPathsToShowScheduleJ30.add("j305_3.sm"); // 4

        String pathJ30 = "/path/to/folder/holding/projects/";
        String fromDatasetJ30 = "J30";

        String pathToBoundsFile = "/path/to/bounds/file/";
        String pathToSaveSchedule = "/path/to/save/schedule";




        int iterations = 30;

        // running statistics
        String pathToSave = "/path/to/save/runstats/";

        int onRun = 0;





        // parameters GA
        int popSize = 1000;
        float interiaW = 1f;
        float r = 0.5f;
        float lr = 0.5f;
        float GR = 0.3f;
        int maxDepth = 5;

        // different selection methods
        String robinhood = "robinHood";
        String roulette = "roulette";
        String elitism = "pureElitism";




        // for statistics
        String fromDataset = fromDatasetJ30;
        List<String> projectPaths = projectPathsToShowScheduleJ30;
        String path = pathJ30;
        int type = 30;

        // recording wins on datasets (amount of times algorithm reach optimal makespan)
        Map<String, Integer> winsInDatasetsEvo = new HashMap<>();
        Map<String, Integer> winsInDatasetsPso = new HashMap<>();
        winsInDatasetsEvo.put(fromDataset, 0);
        winsInDatasetsPso.put(fromDataset, 0);


        // running through the projects
        for (int j=0;j<projectPaths.size();j++) {

            System.out.println("On run: " + onRun);
            onRun+=1;

            String projectpath = projectPaths.get(j);
            Project project = Project.fromFile(path + projectpath, type, pathToBoundsFile);




            // running PSO
            long startTime = System.nanoTime();
            String folder = "/path/to/save/stats";
            Flock.Run flockRun = Flock.run(
                    iterations,
                    popSize,
                    project,
                    0.1f,
                    interiaW,
                    lr, lr,
                    r, r,
                    GR,
                    folder,
                    pathToSaveSchedule
            );
            long endTime = System.nanoTime();
            double elapsedTimeFlock = (endTime - startTime) / 1_000_000_000.0;


            // running GA
            boolean success = false;
            Evolution.Run evoRun = null;
            while (!success) {
                try {
                    startTime = System.nanoTime();

                    evoRun = Evolution.run(
                            type,
                            10,
                            0.5f,
                            popSize,
                            iterations,
                            maxDepth,
                            2,
                            path,
                            robinhood,
                            projectpath,
                            pathToBoundsFile,
                            pathToSaveSchedule
                    );
                    success = true;
                    endTime = System.nanoTime();
                    double elapsedTimeEvo = (endTime - startTime) / 1_000_000_000.0;





                    // saving results
                    String runFolder = "/path/to/save/images";
                    String runFile = project.getRef1() + " .txt";

                    // writing results to a txt file
                    try (FileWriter writer = new FileWriter(runFolder + runFile, true)) {
                        writer.write("time in seconds: PSO: " + elapsedTimeFlock + ", GA: " + elapsedTimeEvo);
                    } catch (IOException e) {
                        System.err.println("An error occurred while writing to the file: " + e.getMessage());
                    }



                } catch (StackOverflowError e) {
                    System.err.println("StackOverflowError occurred. Retrying...");

                    // Prevent infinite retries for unresolvable StackOverflowError
                    success = false;
                }

            }




            // saving amount of wins to an image
            if (evoRun.win)
                winsInDatasetsEvo.put(fromDataset, winsInDatasetsEvo.get(fromDataset) + 1);
            if (flockRun.win)
                winsInDatasetsPso.put(fromDataset, winsInDatasetsPso.get(fromDataset) + 1);

            // writing run stats to a txt
            ChartUtilsHelper.writeTreesToFiles(evoRun, project, pathToSave);

            // charting makespans over time to a png
            ChartUtilsHelper.chartMakespansOverTime(evoRun, flockRun, pathToSave, project);

        }
        // charting wins
        ChartUtilsHelper.makeHistogramOfWins(winsInDatasetsEvo, winsInDatasetsPso, pathToSave, fromDataset + " histogram.png");





    }







    public static void main(String[] args) throws IOException, InterruptedException {

        runProjectTest();


    }













}