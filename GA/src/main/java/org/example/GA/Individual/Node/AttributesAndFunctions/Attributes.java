package org.example.GA.Individual.Node.AttributesAndFunctions;

import org.example.Project.Project;
import org.example.Project.TaskNode;

import java.util.*;



// Hvis legger til nytt attributt:
// legg til attributtet i AttributeMap, så legg til det nye AttributeMap i AttributeNode.applyNode()

public class Attributes {





    // TSC/TPC: The total count of the (immediate & non-immediate) successors/predecessors of an activity.
    public static float TSC(Project project, TaskNode task) {
        int totalSuccessorsCount = 0;
        TaskNode[] tasks = project.getNodeList();

        Queue<Integer> indexesToCheck = new LinkedList<>();
        Set<Integer> uniqueValues = new LinkedHashSet<>();
        indexesToCheck.add(task.getId());
        while (!indexesToCheck.isEmpty()) {
            int nodeIndex = indexesToCheck.poll();
            // Adding all indexes to queque
            indexesToCheck.addAll(tasks[nodeIndex].getSuccessorsIndexes());
            uniqueValues.addAll(tasks[nodeIndex].getSuccessorsIndexes());
        }
        totalSuccessorsCount = uniqueValues.size();
        // normelizing
        float normelizedValue = ((float) 1 / project.getNodeList().length) * totalSuccessorsCount;
        return normelizedValue;
    }


    public static float TPC(Project project, TaskNode task) {
        int totalPredesseccorCount = 0;
        TaskNode[] tasks = project.getNodeList();

        Queue<Integer> indexesToCheck = new LinkedList();
        Set<Integer> uniqueValues = new LinkedHashSet<>();
        indexesToCheck.add(task.getId());
        while (!indexesToCheck.isEmpty()) {
            int nodeIndex = indexesToCheck.poll();
            // Adding all indexes to queque
            indexesToCheck.addAll(tasks[nodeIndex].getPredecessorsIndexes());
            uniqueValues.addAll(tasks[nodeIndex].getPredecessorsIndexes());
        }
        totalPredesseccorCount = uniqueValues.size();
        // normelizing
        float normelizedValue = ((float) 1 / project.getNodeList().length) * totalPredesseccorCount;
        return normelizedValue;
    }






    //ES/EF: The earliest start/finish time for an activity in the precedence feasible schedule calculated by relaxing the resource constraints where each activity is scheduled as early as possible.
    public static float EF(Project project, TaskNode task) {

        int ef = project.findEarliestPathBetween(1, task.getId(), false);

        float normelizedValue = ((float) 1 / project.getLB()) * ef;
        return normelizedValue;
    }


    public static float LS(Project project, TaskNode task) {
       int ls = project.findLongestPathBetween(1, task.getId(), true);

       float normelizedValue = ((float) 1) / project.getUP() * ls;
       return normelizedValue;
    }

    public static float LF(Project project, TaskNode task) {
        int lf = project.findLongestPathBetween(1, task.getId(), false);

        float normelizedValue = ((float) 1) / project.getUP() * lf;
        return normelizedValue;
    }







    public static float ES(Project project, TaskNode task) {
        int es = project.findEarliestPathBetween(1, task.getId(), true);

        float normelizedValue = ((float) 1 / project.getLB()) * es;
        return normelizedValue;
    }


    // RR: The total number of resources required by an activity.
    public static float RR(Project project, TaskNode task) {
        int instances_k_greater_than_0 = 0;
        for (int cost : task.getCosts()) {
            if (cost > 0) {
                instances_k_greater_than_0 += 1;
            }
        }
        // normalizing
        float normelizedValue = ((float) 1 / project.getK()) * instances_k_greater_than_0;
        return normelizedValue;
    }


    // AvgRReq: The average resource requirement of an activity.
    public static float AvgRReq(Project project, TaskNode task) {
        float instances = 0;
        for (int i = 0; i < 4; i += 1) {
            instances += ((float) 1 / project.getRecources()[i]) * task.getCosts()[i];
        }
        // normalizing
        float value = ((float) 1 / project.getK()) * instances;
        return value;
    }


    // MinRReq: The minimum resource requirement of an activity.
    public static float MinRReq(Project project, TaskNode task) {
        float minValue = 99f;
        for (int i = 0; i < 4; i += 1) {
            float value = ((float) 1 / project.getRecources()[i]) * task.getCosts()[i];
            if (value < minValue)
                minValue = value;
        }
        return minValue;
    }

    // MinRReq: The minimum resource requirement of an activity.
    public static float MaxRReq(Project project, TaskNode task) {
        float maxValue = 0f;
        for (int i = 0; i < 4; i += 1) {
            float value = ((float) 1 / project.getRecources()[i]) * task.getCosts()[i];
            if (value > maxValue)
                maxValue = value;
        }
        return maxValue;
    }


}