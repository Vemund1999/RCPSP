package org.example.PSO;


import lombok.Data;
import org.example.GenerationScheme.GenerationScheme;
import org.example.Project.Project;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
@Data
public class Individual {




    private Float[] position;
    private Float[] velocity;
    private int makespan;

    private int lowestRecordedMakespan = 1000;
    private Float[] recordedPositionForLowestMakespan;

    private Project project;

    float maxRpriorityRange;

    float maxRange = 0.1f;





    public Individual(Project project, float maxRpriorityRange) {
        this.project = project;
        this.maxRpriorityRange = maxRpriorityRange;

        int L = project.getNodeList().length;

        recordedPositionForLowestMakespan = new Float[L];
        position = new Float[L];
        velocity = new Float[L];

        randomlyGeneratePositionAndVelocity();
    }


    // initialiserer en tilfeldig posisjon (mellom 0 og 1)
    private void randomlyGeneratePositionAndVelocity() {
        Random random = new Random();
        for (int i=0;i<project.getNodeList().length;i++)
            if (project.getNodeList()[i] != null) {
                position[i] = random.nextFloat();
                velocity[i] = (random.nextFloat() * 2) - 1;
            }
        // makePrioritiesNotSame(); TODO
        calculateAndSetMakespan();
        lowestRecordedMakespan = makespan;
        recordedPositionForLowestMakespan = position;
    }




    public void makePrioritiesNotSame() {
        Random random = new Random();

        // if found same priorities, after having changed priorities,
        // need to check all priorities again to ensure new priorities is not also same
        boolean samePrioritiesNotFound = true;

        while (samePrioritiesNotFound) {
            samePrioritiesNotFound = false;


            for (int i = 0; i < position.length; i++) {
                // go trhough all priorities to check if not same as another priotiry
                ArrayList<Integer> indexesWithSamePriority = new ArrayList<>();
                // track same priorities
                indexesWithSamePriority.add(i);
                for (int j = 0; j < position.length; j++) {
                    if (Objects.equals(position[i], position[j]) && position[i] != null && i != j)
                        // track same priorities
                        indexesWithSamePriority.add(j);
                }
                if (indexesWithSamePriority.size() > 1) {
                    System.out.println("similair priorities");
                    System.out.println(indexesWithSamePriority);
                    System.out.println(Arrays.toString(position));
                    // indicate that there were similair priorities, and after having set the new priorities
                    // all priorities should be checked again to ensure new priorities arent same
                    samePrioritiesNotFound = false;

                    // generate a new priority within a range, for all same priorities
                    for (Integer index : indexesWithSamePriority) {
                        boolean randomBoolean = random.nextBoolean();
                        float newPriority = position[index];
                        if (randomBoolean)
                            newPriority += random.nextFloat() * maxRpriorityRange;
                        if (newPriority > 1)
                            newPriority = 1;
                        else
                            newPriority -= random.nextFloat() * maxRpriorityRange;
                        if (newPriority < 0)
                            newPriority = 0;
                        // set new priority
                        position[index] = newPriority;
                    }
                }
            }


        }
    }




    public void calculateAndSetMakespan(String pathToSaveSchedule) {
        makespan = GenerationScheme.applySerialGenerationScheme(project, position, pathToSaveSchedule);
    }



    public void calculateNewVelocityPositionMakespan(Float[] gBestPosition,
                                                float intertiaW,
                                                float lr1, float lr2,
                                                float r1, float r2)
    {
        calculateNewPosition();
        calculateNewVelocity(
                gBestPosition, intertiaW, lr1, lr2, r1, r2
        );
        calculateAndSetMakespan();
    }




    public void calculateNewPosition() {
        Random random = new Random();

        for (int i=1;i<position.length;i++) {
            position[i] = velocity[i] + position[i];
            if (position[i] > 1f)
                position[i] = (1f-maxRange) + random.nextFloat() * (1f - (1f-maxRange));
            else if (position[i] < 0f)
                position[i] = (0f) + random.nextFloat() * ((0f + maxRange) - (0f));
        }



    }


    public void calculateNewVelocity(Float[] gBestPosition,
                                      float intertiaW,
                                      float lr1, float lr2,
                                      float maxR1, float maxR2
                                      ) {
        Float[] lBestPosition = recordedPositionForLowestMakespan;

        Random random = new Random();
        float r1 = random.nextFloat() * maxR1;
        float r2 = random.nextFloat() * maxR2;

        for (int i=1;i<velocity.length;i++) {
            // calculate velocity
            velocity[i] = intertiaW*velocity[i] + lr1*r1*(lBestPosition[i] - position[i]) + lr2*r2*(gBestPosition[i] - position[i]);
            // ensure within 0,1 range
            if (velocity[i] < -1f)
                position[i] = (-1f) + random.nextFloat() * ((-1f + maxRange) - (0f));
            else if (velocity[i] > 1f)
                position[i] = (1f-maxRange) + random.nextFloat() * (1f - (1f-maxRange));
        }

    }








}
