package org.example.PSO;



import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.Project.Project;
import smile.projection.PCA;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;





public class Flock {

    public static Float[] bestGlobalRecordedPosition;
    public static int bestGlobalRecordedMakeSpan;


    private static void updateBestPosition(Float[] position) {
        for (int i=0; i<position.length; i++)
            bestGlobalRecordedPosition[i] = position[i];
    }


    private static Individual[] initilizePopulation(int popSize,
                                     Project project,
                                     float maxRpriorityRange
    ) {
        Individual[] population = new Individual[popSize];

        for (int i = 0; i < popSize; i++) {
            // initilize individual
            // sets position, velocity, makespan, ensures priorities arent same, sets lBest position and make span
            population[i] = new Individual(project, maxRpriorityRange);
            // check if position is best global position
            if (i == 0) bestGlobalRecordedMakeSpan = population[i].getMakespan();
            if (population[i].getMakespan() < bestGlobalRecordedMakeSpan) {
                bestGlobalRecordedMakeSpan = population[i].getMakespan();
                bestGlobalRecordedPosition = Arrays.copyOf(population[i].getPosition(), population[i].getPosition().length);
            }
        }

        return population;


    }



    @Data
    @NoArgsConstructor
    public static class Run {
        public static int[] gBestInIterations;

        public static int[] avgLBestIterations;

        public static int bestMakespan;

        public boolean win = false;

    }



    public static Float[] useStarOrRingTopology(int i, Individual[] population, float GR) {
        Random random = new Random();

        float rand = random.nextFloat();
        Float[] gBest = null;
        if (rand > GR) {
            int neight1_i = i-1;
            int neight2_i = i+1;
            if (i == 0) neight1_i = population.length-1;
            else if (i == population.length-1) neight2_i = 0;

            if (population[neight1_i].getLowestRecordedMakespan() <= population[neight2_i].getLowestRecordedMakespan() )
                gBest = Arrays.copyOf( population[neight1_i].getRecordedPositionForLowestMakespan(), population[neight1_i].getRecordedPositionForLowestMakespan().length );
            else
                gBest = Arrays.copyOf( population[neight2_i].getRecordedPositionForLowestMakespan(), population[neight2_i].getRecordedPositionForLowestMakespan().length );
        }
        else if (rand <= GR) {
            gBest = Arrays.copyOf( bestGlobalRecordedPosition, bestGlobalRecordedPosition.length );
        }
        assert (gBest != null): "gBest is null. Fix ur code, monkey!";

        return gBest;

    }






    public static Run run(int iterations,
                           int popSize,
                           Project project,
                           float maxRpriorityRange,
                           float inertiaW,
                           float lr1, float lr2,
                           float maxR1, float maxR2,
                           float GR,
                          String folder,
                          String pathToSaveSchedule
    ) {
        assert (0 <= maxR1 && maxR1 <= 1 && 0 <= maxR2 && maxR2 <= 1): "maxR must be same or between 0 and 1";

        Run run = new Run();
        run.gBestInIterations = null;
        run.avgLBestIterations = null;

        int[] gBestInIterations = new int[iterations];
        int[] avgLBestIterations = new int[iterations];



        bestGlobalRecordedMakeSpan = 1000;



        // initilize population
        Individual[] population = initilizePopulation(
                                                 popSize,
                                                 project,
                                                 maxRpriorityRange
                                            );

        for (int t=0;t<iterations;t++) {
            // to track if a makespan lower than gBets is found
            int lowestMakespanT = 1000;
            int lowestMakespanTIndividualI = 0;
            int avgMakespan = 0;
            int avgLMakespan = 0;
            avgLBestIterations[t] = 0;
            // updating the position of all individuals
            for (int i=0;i<population.length;i++) {
                // determine which topology to use
                Float[] gBest = useStarOrRingTopology(i, population, GR);
                // calculate individuals velocity
                population[i].calculateNewVelocity(gBest, inertiaW, lr1, lr2, maxR1, maxR2);
                // calculate individual's position
                population[i].calculateNewPosition();
                // calculate new make span
                population[i].calculateAndSetMakespan(pathToSaveSchedule);
                avgMakespan += population[i].getMakespan();
                // if new make span is lower, update lBest for individual
                if (population[i].getMakespan() < population[i].getLowestRecordedMakespan()) {
                    population[i].setLowestRecordedMakespan( population[i].getMakespan() );
                    Float[] copy = new Float[ population[i].getPosition().length ];
                    for (int a=0;a<copy.length;a++)
                        copy[a] = population[i].getPosition()[a];
                    population[i].setRecordedPositionForLowestMakespan(  copy );
                }
                avgLBestIterations[t] += population[i].getLowestRecordedMakespan();

                // checking if individual's make span is global best
                if (population[i].getLowestRecordedMakespan() < lowestMakespanT) {
                    lowestMakespanT = population[i].getLowestRecordedMakespan();
                    lowestMakespanTIndividualI = i;
                }

                // make stats for scatter plot
                int middleIteration = iterations / 2;
                if (t == 1 || t == middleIteration || t == (iterations-1)) {
                    if (i == 3) {
                        String filename = project.getRef1();
                        Float[] copy = Arrays.copyOf(population[i].getRecordedPositionForLowestMakespan(), population[i].getRecordedPositionForLowestMakespan().length);

                        try {
                            // Create a FileWriter in append mode and wrap it with a BufferedWriter
                            FileWriter fileWriter = new FileWriter(folder + filename, true);  // true for append mode
                            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                            // Write some content to the file
                            bufferedWriter.write("Lbest of ind 3: " + Arrays.toString(copy) + ", makespan: " + population[i].getLowestRecordedMakespan() + ", deviation: " + (population[i].getLowestRecordedMakespan() - project.getLB()) + "\n");

                            // Close the BufferedWriter
                            bufferedWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }



                }




            }
            avgLBestIterations[t] = avgLBestIterations[t] / popSize;
            // updating global best
            if (lowestMakespanT < bestGlobalRecordedMakeSpan) {
                bestGlobalRecordedMakeSpan = population[lowestMakespanTIndividualI].getLowestRecordedMakespan();
                bestGlobalRecordedPosition = Arrays.copyOf(population[lowestMakespanTIndividualI].getRecordedPositionForLowestMakespan(), population[lowestMakespanTIndividualI].getRecordedPositionForLowestMakespan().length);
            }

            gBestInIterations[t] = bestGlobalRecordedMakeSpan;






        }



        run.gBestInIterations = gBestInIterations;
        run.avgLBestIterations = avgLBestIterations;

        String filename = project.getRef1();

        try {
            // Create a FileWriter in append mode and wrap it with a BufferedWriter
            FileWriter fileWriter = new FileWriter(folder + filename, true);  // true for append mode
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Write some content to the file
            bufferedWriter.write("Gbest: " + Arrays.toString(bestGlobalRecordedPosition) + ", makespan: " + bestGlobalRecordedMakeSpan + ", deviation: " + (project.getLB() - bestGlobalRecordedMakeSpan + " \n"));

            // Close the BufferedWriter
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (bestGlobalRecordedMakeSpan - project.getLB() <= 0)
            run.win = true;

        run.bestMakespan = bestGlobalRecordedMakeSpan;


        return run;


    }







}
