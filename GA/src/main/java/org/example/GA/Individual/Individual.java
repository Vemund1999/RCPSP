package org.example.GA.Individual;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.GA.Individual.Node.AttributeNode;
import org.example.GA.Individual.Node.Node;
import org.example.Project.Project;
import org.example.Project.TaskNode;


@Data
@AllArgsConstructor
public class Individual {

    private Node root;
    private float fitness;













    public Individual(Node root) {
        this.root = root;
    }


    // returns a list of which tasks are priority order of tasks
    // the integers in the list are the id(s) of the tasks
    // highest priority to the left, lowest to the right
    public Float[] applyToProject(Project project) {
        int L = project.getNodeList().length;
        Float[] priorityList = new Float[L]; // using wrapper class to have the default value be null, instead of 0f
        for (int i=0; i<L;i++)
            if (project.getNodeList()[i] != null)
                priorityList[i] = applyToTask(root, project, project.getNodeList()[i]);
        return priorityList;
    }



    private float applyToTask(Node node, Project project, TaskNode task) {
        if (node.getClass().isInstance(new AttributeNode()))
            return node.applyNode(project, task);

        float valueOne = applyToTask(node.getLeft(), project, task);
        float valueTwo = applyToTask(node.getRight(), project, task);
        return node.applyNode(valueOne, valueTwo);
    }




    public String printTree() {
        StringBuilder sb = new StringBuilder();
        printTreeRecursive(sb, "", "", root);
        return sb.toString();
    }


    private void printTreeRecursive(StringBuilder sb, String padding, String pointer, Node node) {
        if (node != null) {
            sb.append(padding);
            sb.append(pointer);
            sb.append(node.getValue());
            sb.append("\n");

            StringBuilder paddingBuilder = new StringBuilder(padding);
            paddingBuilder.append("│  ");

            String paddingForBoth = paddingBuilder.toString();
            String pointerForRight = "└──";
            String pointerForLeft = (node.getRight() != null) ? "├──" : "└──";


            printTreeRecursive(sb, paddingForBoth, pointerForLeft, node.getLeft());
            printTreeRecursive(sb, paddingForBoth, pointerForRight, node.getRight());
        }
    }







}
