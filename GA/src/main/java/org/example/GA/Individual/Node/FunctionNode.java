package org.example.GA.Individual.Node;

import lombok.Data;
import org.example.GA.Individual.Node.AttributesAndFunctions.Functions;
import org.example.GA.Individual.Node.AttributesAndFunctions.FunctionsMap;


@Data
public class FunctionNode extends Node<Float, Float, FunctionsMap> {



    @Override
    public float applyNode(Float a, Float b) {
        return switch (value) {
            case add -> Functions.add(a, b);
            case mul -> Functions.mul(a, b);
            case sub -> Functions.sub(a, b);
            case div -> Functions.div(a, b);
            case max -> Functions.max(a, b);
            case min -> Functions.min(a, b);
            case neg1 -> Functions.neg1(a);
        };
    }

}
