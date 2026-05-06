package org.hobynye.thankyoumatcher.solver;

import java.util.*;

public class MaxFlowSolver {

    private final List<List<FlowEdge>> graph;

    public MaxFlowSolver(int nodeCount) {
        this.graph = new ArrayList<>();

        for (int i = 0; i < nodeCount; i++) {
            graph.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int capacity) {
        FlowEdge forward = new FlowEdge(from, to, capacity);
        FlowEdge backward = new FlowEdge(to, from, 0);

        forward.setReverse(backward);
        backward.setReverse(forward);

        graph.get(from).add(forward);
        graph.get(to).add(backward);
    }

    public int maxFlow(int source, int sink) {
        int totalFlow = 0;

        while (true) {
            FlowEdge[] parent = new FlowEdge[graph.size()];
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(source);

            while (!queue.isEmpty() && parent[sink] == null) {
                int current = queue.poll();

                for (FlowEdge edge : graph.get(current)) {
                    if (parent[edge.getTo()] == null
                            && edge.getTo() != source
                            && edge.remainingCapacity() > 0) {
                        parent[edge.getTo()] = edge;
                        queue.add(edge.getTo());
                    }
                }
            }

            if (parent[sink] == null) {
                break;
            }

            int pathFlow = Integer.MAX_VALUE;

            for (FlowEdge edge = parent[sink]; edge != null; edge = parent[edge.getFrom()]) {
                pathFlow = Math.min(pathFlow, edge.remainingCapacity());
            }

            for (FlowEdge edge = parent[sink]; edge != null; edge = parent[edge.getFrom()]) {
                edge.addFlow(pathFlow);
            }

            totalFlow += pathFlow;
        }

        return totalFlow;
    }

    public List<FlowEdge> getEdgesFrom(int node) {
        return graph.get(node);
    }
}