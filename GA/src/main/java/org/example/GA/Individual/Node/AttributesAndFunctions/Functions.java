package org.example.GA.Individual.Node.AttributesAndFunctions;



// Hvis legger til ny funksjon:
// legg til attributtet i FunctionsMap, så legg til det nye FunctionsMap i FunctionNode.applyNode()


public class Functions {



    public static float add(float a, float b) {
        return a + b;
    }


    public static float mul(float a, float b) {
        return a * b;
    }


    public static float sub(float a, float b) {
        return a - b;
    }



    public static float div(float a, float b) {
        if (b > 0)
            return a / b;
        return 0;
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static float min(float a, float b) {
        return Math.min(a, b);
    }


    public static float neg1(float a) {
        return -1*a;
    }










}
