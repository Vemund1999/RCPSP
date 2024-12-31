package org.example.GA.Individual.Node;


import lombok.Data;

import java.util.Random;

@Data
public abstract class Node<T, A, AttributeOrFunction> {

    // used to identify nodes with similair trees
    protected int id = new Random().nextInt(1000000);

    protected Node left;
    protected Node right;

    protected AttributeOrFunction value;

    public abstract float applyNode(T a, A b);


    private static void setId() {

    }

}
