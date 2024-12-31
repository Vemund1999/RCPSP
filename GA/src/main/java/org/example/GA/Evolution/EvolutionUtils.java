package org.example.GA.Evolution;

import org.example.GA.Individual.Individual;
import org.example.GA.Individual.Node.AttributeNode;
import org.example.GA.Individual.Node.AttributesAndFunctions.AttributeMap;
import org.example.GA.Individual.Node.Node;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class EvolutionUtils {










    public static Map<AttributeMap, Integer> initilizeCountAttributeMap() {
        Map<AttributeMap, Integer> attributeCounts = new HashMap<>();
        for (AttributeMap attribute : AttributeMap.values())
            attributeCounts.put(attribute, 0);
        return attributeCounts;
    }




    public static Map<AttributeMap, Integer> countAttributesOfIndividual(Individual individual) {

        Map<AttributeMap, Integer> attributeCounts = initilizeCountAttributeMap();

        Queue<Node> nodes = new LinkedList<>();
        nodes.add( individual.getRoot() );
        while (!nodes.isEmpty()) {

            Node node = nodes.poll();

            if (node instanceof AttributeNode) {
                int currentCount = attributeCounts.get(node.getValue());
                int incrementedCount = currentCount += 1;
                attributeCounts.put(((AttributeNode) node).getValue(), incrementedCount);
                continue;
            }

            nodes.add( node.getLeft() );
            nodes.add( node.getRight() );
        }
        return attributeCounts;
    }




    public static Map<AttributeMap, Integer> countAttributesInPopulation(Individual[] individuals) {
        Map<AttributeMap, Integer> attributeCounts = initilizeCountAttributeMap();
        for (Individual individual : individuals) {
            Map<AttributeMap, Integer> attributeCountsIndividual = countAttributesOfIndividual(individual);
            for (Map.Entry<AttributeMap, Integer> entry : attributeCountsIndividual.entrySet()) {
                int currentValue = attributeCounts.get(entry.getKey());
                int incrementValue = entry.getValue();
                int newValue = currentValue + incrementValue;
                attributeCounts.put( entry.getKey(), newValue );
            }
        }
        return attributeCounts;
    }










    public static void printFitnesses(Individual[] individuals) {
        for (Individual individual : individuals) {
            System.out.println(individual);
            System.out.println(individual.getFitness());
        }
    }


    public static float getAvePopFitness(Individual[] individuals) {
        float totalFitness = 0;
        for (Individual individual : individuals)
            totalFitness += individual.getFitness();
        return  totalFitness / individuals.length;
    }

    public static int fittestIndex(Individual[] individuals) {
        float min = 100000;
        int mostFitIndex = 0;

        for (int i = 0; i < individuals.length; i++) {
            if (individuals[i].getFitness() < min) {
                min = individuals[i].getFitness();
                mostFitIndex = i;
            }
        }
        return mostFitIndex;
    }


    public static int leastFittestIndex(Individual[] individuals) {
        float max = 0;
        int leastFitIndex = 0;

        for (int i = 0; i < individuals.length; i++) {
            if (individuals[i].getFitness() > max) {
                max = individuals[i].getFitness();
                leastFitIndex = i;
            }
        }
        return leastFitIndex;
    }





}
