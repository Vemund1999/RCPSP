package org.example.GA.Utils;

import org.example.GA.Individual.Individual;
import org.example.GA.Individual.Node.AttributeNode;
import org.example.GA.Individual.Node.AttributesAndFunctions.AttributeMap;
import org.example.GA.Individual.Node.AttributesAndFunctions.FunctionsMap;
import org.example.GA.Individual.Node.FunctionNode;
import org.example.GA.Individual.Node.Node;

import java.util.*;


public class IndividualUtils {

    private final static Random random = new Random();
    private final static FunctionsMap[] functions = FunctionsMap.values();
    private final static AttributeMap[] attributes = AttributeMap.values();







    // https://www.sciencedirect.com/science/article/pii/S0957417422002196
    // 3.3 Initialization

    public static Individual generateTree(int minDepth, int maxDepth) {
        Node root = grow(0, minDepth, maxDepth);
        return new Individual(root);
    }


    public static int findHeight(Node node) {
        if (node instanceof AttributeNode)
            return 1;

        int leftHeight = findHeight(node.getLeft());
        int rightHeight = findHeight(node.getRight());

        return Math.max(leftHeight, rightHeight) + 1;
    }


    private static Node grow(int depth, int minDepth, int maxDepth) {
        if (depth >= minDepth && (depth == maxDepth || random.nextBoolean()))
            return generateAttributeNode();

        Node node = generateFunctionNode();
        node.setLeft( grow(depth+1, minDepth, maxDepth) );
        node.setRight( grow(depth+1, minDepth, maxDepth) );
        return node;
    }




    private static Node generateAttributeNode() {
        AttributeNode node = new AttributeNode();
        int index = random.nextInt(attributes.length);
        AttributeMap value = attributes[index];
        node.setValue(value);
        return node;
    }

    private static Node generateFunctionNode() {
        FunctionNode node = new FunctionNode();
        int index = random.nextInt(functions.length);
        FunctionsMap value = functions[index];
        node.setValue(value);
        return node;
    }





    public static Node makeDeepCopy(Node node) {

        Node copy;

        if (node instanceof FunctionNode) {
            copy = new FunctionNode();
            copy.setId(node.getId());
            copy.setValue(node.getValue());
            copy.setRight(makeDeepCopy(node.getRight()));
            copy.setLeft(makeDeepCopy(node.getLeft()));
            return copy;
        }
        // else:  attribute node
        copy = new AttributeNode();
        copy.setId(node.getId());
        copy.setValue(node.getValue());

        return copy;
    }





}
