package org.example.Project;

import lombok.Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Data
public class Project {

    private int id;
    private int rootNodeIndex;
    private TaskNode[] nodeList;
    private int[] recources = new int[4]; // Array to hold resource availabilities
    private int K;
    private int maxEF;

    private int UP, LB;

    private String Ref1;




    public static Project fromFile(String filename, int type, String pathToBoundsFile) throws IOException {
        return new Project().parseFile(filename, type, pathToBoundsFile);
    }

    // parsing a project file, and making a network from the file
    public Project parseFile(String filePath, int type, String pathToBoundsFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;



        boolean isProjectInfoSection = false;
        boolean isPrecedenceRelationsSection = false;
        boolean isRequestsDurationsSection = false;
        boolean isResourceAvailabilitySection = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("************************************************************************")) {
                continue; // Skip empty lines and separators
            }

            if (line.startsWith("PROJECT INFORMATION:")) {
                isProjectInfoSection = true;
                continue;
            } else if (line.startsWith("PRECEDENCE RELATIONS:")) {
                isProjectInfoSection = false;
                isPrecedenceRelationsSection = true;
                continue;
            } else if (line.startsWith("REQUESTS/DURATIONS:")) {
                isPrecedenceRelationsSection = false;
                isRequestsDurationsSection = true;
                continue;
            } else if (line.startsWith("RESOURCEAVAILABILITIES:")) {
                isRequestsDurationsSection = false;
                isResourceAvailabilitySection = true;
                continue;
            }

            if (isProjectInfoSection) {
                if (!line.startsWith("pronr.")) { // Skip header
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length == 6) {
                        int projectNumber = Integer.parseInt(parts[0]);
                        int numberOfJobs = Integer.parseInt(parts[1]);
                        int releaseDate = Integer.parseInt(parts[2]);
                        int dueDate = Integer.parseInt(parts[3]);
                        int tardinessCost = Integer.parseInt(parts[4]);
                        int mpmTime = Integer.parseInt(parts[5]);

                        id = projectNumber;
                        maxEF = mpmTime;
                        nodeList = new TaskNode[type + 2 + 1];
                    }
                }
            } else if (isPrecedenceRelationsSection) {
                if (!line.startsWith("jobnr.")) { // Skip header
                    String[] parts = line.trim().split("\\s+");
                    int jobNumber = Integer.parseInt(parts[0]);
                    int modes = Integer.parseInt(parts[1]);
                    int numberOfSuccessors = Integer.parseInt(parts[2]);
                    ArrayList<Integer> successors = new ArrayList<>();
                    for (int i = 3; i < parts.length; i++) {
                        successors.add(Integer.parseInt(parts[i]));
                    }
                    TaskNode task = new TaskNode();
                    task.setId(jobNumber);
                    task.setMode(modes);
                    task.setSuccessorsIndexes(successors);
                    nodeList[jobNumber] = task;
                    if (jobNumber == 1)
                        rootNodeIndex = jobNumber;


                }
            } else if (isRequestsDurationsSection) {
                if (!line.startsWith("jobnr.")) { // Skip header
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 6) {
                        int jobNumber = Integer.parseInt(parts[0]);
                        int mode = Integer.parseInt(parts[1]);
                        int duration = Integer.parseInt(parts[2]);
                        ArrayList<Integer> resourceRequests = new ArrayList<>();
                        for (int i = 0; i < 4; i++) {
                            nodeList[jobNumber].getCosts()[i] = Integer.parseInt(parts[3 + i]);
                        }
                        nodeList[jobNumber].setMakeSpan(duration);

                    }
                }
            } else if (isResourceAvailabilitySection) {
                if (!line.startsWith("R")) {
                    String[] parts = line.trim().split("\\s+");
                    for (int i = 0; i < parts.length; i++) {
                        int r = Integer.parseInt(parts[i]);
                        recources[i] = r;
                        K += Integer.parseInt(parts[i]);
                    }

                }


            }
        }


        reader.close();

        calculateOverviewOfPredecessorNetwork();
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        findBounds(fileName, pathToBoundsFile);


        return this;
    }






    private void findBounds(String targetRef1, String pathToBoundsFile) {





        int maxValue = 0;
        int minValue = 100;
        try (BufferedReader br = new BufferedReader(new FileReader(pathToBoundsFile))) {
            String line;
            int ref1Index = -1, lbValueIndex = -1, lbTimeIndex = -1, ubValueIndex = -1, ubTimeIndex = -1;
            boolean dataSectionStarted = false;

            // Iterate through lines to find the header
            while ((line = br.readLine()) != null) {
                // Look for the header row that starts with "ID;" to start parsing the data section
                if (line.startsWith("ID;")) {
                    dataSectionStarted = true;
                    String[] headers = line.split(";");

                    // Find column indices in the header row
                    for (int i = 0; i < headers.length; i++) {
                        switch (headers[i].trim()) {
                            case "Ref1":
                                ref1Index = i;
                                break;
                            case "LB value":
                                lbValueIndex = i;
                                break;
                            case "LB time":
                                lbTimeIndex = i;
                                break;
                            case "UB value":
                                ubValueIndex = i;
                                break;
                            case "UB time":
                                ubTimeIndex = i;
                                break;
                        }
                    }
                    // Proceed to the next line since header is processed
                    break;
                }
            }

            // Validate that all required columns were found in the header
            if (!dataSectionStarted || ref1Index == -1 || lbValueIndex == -1 || lbTimeIndex == -1 || ubValueIndex == -1 || ubTimeIndex == -1) {
                return;
            }

            // Read data rows and search for the target Ref1
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");

                // Check if this row matches the target Ref1
                if (values.length > ref1Index && values[ref1Index].trim().equals(targetRef1)) {
                    this.Ref1 = values[ref1Index].trim();
                    String lbValue = values[lbValueIndex].trim();
                    String lbTime = values[lbTimeIndex].trim();
                    String ubValue = values[ubValueIndex].trim();
                    String ubTime = values[ubTimeIndex].trim();

                    String[] valuesInRow = new String[4];
                    valuesInRow[0] = lbValue;
                    valuesInRow[1] = lbTime;
                    valuesInRow[2] = ubValue;
                    valuesInRow[3] = ubTime;


                    for (String value : valuesInRow) {
                        int valueInt = Integer.parseInt(value);
                        if (valueInt > maxValue)
                            maxValue = valueInt;
                        if (valueInt > 0 && valueInt < minValue)
                            minValue = valueInt;
                    }
                    this.LB = minValue;
                    this.UP = maxValue;

                    return;
                }
            }







        } catch (IOException e) {
            e.printStackTrace();
        }

    }







    private void calculateOverviewOfPredecessorNetwork() {
        Queue<Integer> queue = new LinkedList();
        queue.add(rootNodeIndex);
        while (!queue.isEmpty()) {
            int nodeIndex = queue.poll();
            for (int successor : nodeList[nodeIndex].getSuccessorsIndexes()) {
                nodeList[successor].getPredecessorsIndexes().add(nodeIndex);
                queue.add(successor);
            }
        }
    }






    List<List<Integer>> allPaths = new ArrayList<>();
    List<Integer> currentPath = new ArrayList<>();
    public int findEarliestPathBetween(int startIndex, int targetJob, boolean es) { //false - find the earliest finishtime

        findAllPaths(startIndex, targetJob, currentPath, allPaths);

        int bestMakespan = getUP();
        List<Integer> bestPath = new ArrayList<>();
        for (List<Integer> path : allPaths) {
            int pathMakespan = 0;
            for (int nodeIndex : path) {
                pathMakespan += nodeList[nodeIndex].getMakeSpan();
            }
            if (pathMakespan < bestMakespan) {
                bestMakespan = pathMakespan;
                bestPath = path;
            }

        }
        allPaths = new ArrayList<>();
        currentPath = new ArrayList<>();


        if (es) {
            bestMakespan = bestMakespan - nodeList[targetJob].getMakeSpan();
        }


        return bestMakespan;
    }


    public int findLongestPathBetween(int startIndex, int targetJon, boolean ls) { // false - find the latest finishtime

        findAllPaths(startIndex, targetJon, currentPath, allPaths);

        int bestMakespan = getLB();
        List<Integer> bestPath = new ArrayList<>();
        for (List<Integer> path : allPaths) {
            int pathMakespan = 0;
            for (int nodeIndex : path) {
                pathMakespan += nodeList[nodeIndex].getMakeSpan();
            }
            if (pathMakespan > bestMakespan) {
                bestMakespan = pathMakespan;
                bestPath = path;
            }

        }
        allPaths = new ArrayList<>();
        currentPath = new ArrayList<>();


        if (ls) {
            bestMakespan = bestMakespan - nodeList[targetJon].getMakeSpan();
        }


        return bestMakespan;
    }



    private void findAllPaths(int startIndex, int targetJob, List<Integer> currentPath, List<List<Integer>> allPaths) {
        // Get the current node
        TaskNode currentNode = nodeList[startIndex];


        // Add the current job to the current path
        currentPath.add(currentNode.getId());

        // If we reached the target job, add the current path to allPaths
        if (currentNode.getId() == targetJob) {
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            // Continue DFS for each successor
            for (int successor : currentNode.getSuccessorsIndexes()) {
                findAllPaths(successor, targetJob, currentPath, allPaths);
            }
        }

        // Backtrack: remove the current job from the current path
        currentPath.remove(currentPath.size() - 1);
    }











}
