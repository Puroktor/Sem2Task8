package ru.vsu.cs.skofenko;

import java.util.List;
import java.util.Scanner;

public class GraphTransformations {

    public static WGraph buildGraph(String text) {
        Scanner sc = new Scanner(text);
        int n = sc.nextInt(), m = sc.nextInt();
        WGraph wGraph = new WGraph(n);
        for (int i = 0; i < m; i++) {
            int from = sc.nextInt(), to = sc.nextInt(), l = sc.nextInt();
            wGraph.addEdge(from, to, l);
        }
        return wGraph;
    }

    public static String toDot(WGraph graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("strict graph{\n");
        for (int i = 0; i < graph.numOfVert(); i++) {
            List<WGraph.Pair> list = graph.getEdges(i);
            if (list.isEmpty())
                sb.append(i).append("\n");
            else {
                for (WGraph.Pair pair : list)
                    sb.append(i).append("--").append(pair.to).append("[fontcolor=\"red\" fontsize=\"10.0\" label=\"").
                            append(pair.l).append("\"]\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static String answerToDot(WGraph graph) {
        if (graph.getMin() == -1)
            throw new UnsupportedOperationException();
        StringBuilder sb = new StringBuilder();
        sb.append("digraph {\n");
        sb.append(graph.getMin()).append(" [color = blue]\n");
        for (int i = 0; i < graph.getBestWay().length; i++) {
            if (i != graph.getMin())
                sb.append(i).append(" -> ").append(graph.getBestWay()[i].to).append("[fontcolor=\"red\" fontsize=\"10.0\" label=\"").
                        append(graph.getBestWay()[i].l).append("\"]\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
