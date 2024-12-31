package org.example.GA.Individual.Node;

import lombok.Data;
import org.example.GA.Individual.Node.AttributesAndFunctions.AttributeMap;
import org.example.GA.Individual.Node.AttributesAndFunctions.Attributes;
import org.example.Project.Project;
import org.example.Project.TaskNode;


@Data
public class AttributeNode extends Node<Project, TaskNode, AttributeMap> {



    @Override
    public float applyNode(Project project, TaskNode taskNode) {
        return switch (value) {
            case TSC -> Attributes.TSC(project, taskNode);
            case TPC -> Attributes.TPC(project, taskNode);
            case EF -> Attributes.EF(project, taskNode);
            case ES -> Attributes.ES(project, taskNode);
            case RR -> Attributes.RR(project, taskNode);
            case AvgRReq -> Attributes.AvgRReq(project, taskNode);
            case MaxRReq -> Attributes.MaxRReq(project, taskNode);
            case MinRReq -> Attributes.MinRReq(project, taskNode);
        };
    }
}
