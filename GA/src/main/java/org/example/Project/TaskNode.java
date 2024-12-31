package org.example.Project;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;


@Data
public class TaskNode {


    private int id;
    private int[] costs = new int[4];
    private int makeSpan;
    private ArrayList<Integer> successorsIndexes = new ArrayList<>();
    private HashSet<Integer> predecessorsIndexes = new HashSet<>();
    private int mode;








}
