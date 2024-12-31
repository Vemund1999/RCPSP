package org.example.GenerationScheme;


import org.example.Project.Project;
import org.example.Project.TaskNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



public class GenerationScheme {


    static class Pair {
        Float value;
        Integer relatedItem;

        Pair(Float value, Integer relatedItem) {
            this.value = value;
            this.relatedItem = relatedItem;
        }
    }

    // sorts the ids
    private static ArrayList<Integer> getSortedPriorityIds(Project project, Float[] priorityList) {
        List<Integer> taskIds = IntStream.rangeClosed(0, project.getNodeList().length)
                .boxed()
                .collect(Collectors.toList());

        List<Pair> combined = new ArrayList<>();
        int indexesInTaskIdsNotNull = 0;
        for (int i = 0; i < priorityList.length; i++) {
            if (priorityList[i] != null && taskIds.get(i) != null) {
                combined.add(new Pair(priorityList[i], taskIds.get(i)));
                indexesInTaskIdsNotNull += 1;
            }
        }

        Collections.sort(combined, Comparator.comparingDouble(pair -> -pair.value));

        ArrayList<Integer> sortedIds = new ArrayList<>();
        for (int i = 0; i < indexesInTaskIdsNotNull; i++)
            sortedIds.add(combined.get(i).relatedItem);

        return sortedIds;
    }







    private static Map<Integer, List<Integer>> getTimeHorizonSchedualing(Project project) {
        Map<Integer, List<Integer>> timeUnitMap = new HashMap<>();

        int timeMax = 0;
        for (TaskNode task : project.getNodeList())
            if (task != null) timeMax += task.getMakeSpan();

        for (int i=0; i<timeMax; i++) {
            timeUnitMap.put(i, new ArrayList<>());
        }

        return timeUnitMap;
    }

    private static Map<Integer, List<Integer>> getTimeHorizonResources(Project project) {
        Map<Integer, List<Integer>> timeUnitMap = new HashMap<>();

        int timeMax = 0;
        for (TaskNode task : project.getNodeList())
            if (task != null) timeMax += task.getMakeSpan();

        for (int i=0; i<timeMax; i++) {
            timeUnitMap.put(i, new ArrayList<>());
            timeUnitMap.get(i).add(project.getRecources()[0]);
            timeUnitMap.get(i).add(project.getRecources()[1]);
            timeUnitMap.get(i).add(project.getRecources()[2]);
            timeUnitMap.get(i).add(project.getRecources()[3]);

        }

        return timeUnitMap;
    }


    private static int getTimeHorizon(Project project) {
        int timeHorizon = 0;
        for (TaskNode task : project.getNodeList())
            if (task != null)
                timeHorizon += task.getMakeSpan();
        return timeHorizon;
    }





    // returns makespan
    public static int applySerialGenerationScheme(Project project, Float[] priorityList, String pathToSaveSchedule) {

        ArrayList<Integer> sortedIds = getSortedPriorityIds(project, priorityList);
        Map<Integer, List<Integer>> avalibleResourcesOverTime = getTimeHorizonResources(project);
        Map<Integer, List<Integer>> taskSchedualingOverTime = getTimeHorizonSchedualing(project);

        List<Integer> idsWithParentsWith0Makespan = listIdsWithParentsWith0Makespan(project);

        int idToCheckOfSortedIds = 0;
        int tasksSchedualed = 0;
        int tasksToSchedual = sortedIds.size();
        int timeHorizon = getTimeHorizon(project);
        while (true) {

            // if done with last task, begin again at first task
            if (idToCheckOfSortedIds >= sortedIds.size())
                idToCheckOfSortedIds = 0;
            // pick task
            TaskNode task = project.getNodeList()[ sortedIds.get(idToCheckOfSortedIds) ];

            // if not all preds schedualed, go to next task
            if (!hasAllpredesecorsBeenSchedualed(task.getId(), taskSchedualingOverTime, project)) {
                idToCheckOfSortedIds++;
                continue;
            }

            // finding earliest possible starting respecting predessecor constrains
            int t = getTimeUnitOfEarliestFinishTimeForAllPredecessors(task, taskSchedualingOverTime, project);
            // finding earliest possible starting respecting resource constrains

            int makespanLooped = 0;
            int earliestPossibleStartime = 0;
            for (int timeUnit=t;timeUnit<timeHorizon;timeUnit++) {
                List<Integer> resourceUsages = avalibleResourcesOverTime.get(timeUnit);

                for (int j = 0; j < 4; j++) {
                    if (resourceUsages.get(j) - task.getCosts()[j] < 0) {
                        makespanLooped = 0;
                        break;
                    }
                }
                if (makespanLooped == task.getMakeSpan()) {
                    earliestPossibleStartime = timeUnit - makespanLooped;
                    break; // Breaks out of the outer loop
                }
                makespanLooped += 1;
            }

            // schedualing task at earliest possible point
            int max;
            if (task.getMakeSpan() == 0)
                max = 1 + earliestPossibleStartime;
            else
                max = task.getMakeSpan() + earliestPossibleStartime;

            for (int timeUnit=earliestPossibleStartime;timeUnit<max;timeUnit++) {
                // adding to schedual
                taskSchedualingOverTime.get(timeUnit).add(task.getId());

                // removing resources
                for (int i=0;i<4;i++) {
                    int resourceUsage = avalibleResourcesOverTime.get(timeUnit).get(i);
                    avalibleResourcesOverTime.get(timeUnit).set(i, resourceUsage - task.getCosts()[i]);
                }
            }

            // mark task as schedualed
            tasksSchedualed += 1;
            sortedIds.remove(idToCheckOfSortedIds);
            // if all tasks schedualed, break
            if (tasksSchedualed == tasksToSchedual)
                break;

            idToCheckOfSortedIds++;
        }

        saveScheduleToFile(taskSchedualingOverTime, pathToSaveSchedule);
        return getMakeSpanOfSchedual(taskSchedualingOverTime, project);


    }


    private static void saveScheduleToFile(Map<Integer, List<Integer>> taskSchedualingOverTime, String pathToSaveSchedule) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathToSaveSchedule))) {
            // Loop through the Map and write each entry to the file
            for (Map.Entry<Integer, List<Integer>> entry : taskSchedualingOverTime.entrySet()) {
                int key = entry.getKey();
                List<Integer> values = entry.getValue();

                // Format: Key -> [value1, value2, value3]
                writer.write(key + " -> " + values);
                writer.newLine(); // Move to the next line
            }

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }





    private static List<Integer> listIdsWithParentsWith0Makespan(Project project) {
        ArrayList<Integer> idsWith0Makespan = new ArrayList<>();
        for (TaskNode task : project.getNodeList())
            if (task != null && task.getMakeSpan() == 0)
                idsWith0Makespan.addAll(task.getSuccessorsIndexes());
        return idsWith0Makespan;

    }


    private static int getMakeSpanOfSchedual(Map<Integer, List<Integer>> taskSchedualingOverTime, Project project) {
        int makespan = 0;
        for (int i=0;i<taskSchedualingOverTime.size();i++) {
            if (taskSchedualingOverTime.get(i).isEmpty()) //antar at det aldri ikke kan være en scheduert task
                return makespan;

            boolean allTasksHave0Makespan = true;
            for (int taskId : taskSchedualingOverTime.get(i))
                if (project.getNodeList()[taskId].getMakeSpan() != 0)
                    allTasksHave0Makespan = false;

            if (!allTasksHave0Makespan)
                makespan+=1;
        }
        return  makespan;
    }


    private static int getTimeUnitOfEarliestFinishTimeForAllPredecessors(TaskNode task, Map<Integer, List<Integer>> taskSchedualingOverTime, Project project) {

        if (task.getPredecessorsIndexes().isEmpty())
            return 0;

        HashSet<Integer> foundPredessecors = new HashSet<>();
        int time = 0;

        // looping schedualed tasks
        for (Map.Entry<Integer, List<Integer>> entry : taskSchedualingOverTime.entrySet()) {
            time = entry.getKey();
            List<Integer> schedualedIds = entry.getValue();
            // for each task at a timeunit
            if (schedualedIds.isEmpty())
                return time;
            boolean currentUnitHasNoPredeseccors = true;
            for (int id : schedualedIds) {
                // check if task.preds contains task

                if (task.getPredecessorsIndexes().contains(id)) {
                    foundPredessecors.add(id); // if so, add to preds in schedule found
                    currentUnitHasNoPredeseccors = false;
                }

                boolean cont1 = currentUnitHasNoPredeseccors && id == schedualedIds.get( schedualedIds.size() - 1 );
                boolean con2 = foundPredessecors.containsAll( task.getPredecessorsIndexes() );
                boolean cond3 = !foundPredessecors.isEmpty();

                // if all preds are found, and the current time unit doesn't contain a pred, then return that value
                boolean cond = cont1 && con2 && cond3;
                if (cond) {
                    return time;
                }
            }
        }
        return 0;
    }




    private static boolean hasAllpredesecorsBeenSchedualed(int id, Map<Integer, List<Integer>> taskSchedualingOverTime, Project project) {

        HashSet<Integer> predessecorsSchedaled = new HashSet<>();
        TaskNode task = project.getNodeList()[ id ];

        taskSchedualingOverTime.forEach((timeUnit, schedualedIds) -> {
            for (int idSchedualed : schedualedIds) {
                if (task.getPredecessorsIndexes().contains(idSchedualed)) {
                    predessecorsSchedaled.add(idSchedualed);
                }
            }

        });
        boolean condition = ( predessecorsSchedaled.containsAll(task.getPredecessorsIndexes()) || (predessecorsSchedaled.isEmpty() ) && task.getPredecessorsIndexes().isEmpty());

        return condition;
    }


}