package org.hobynye.thankyoumatcher.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class MinCostMaxFlowSolver {

    private final List<List<CostFlowEdge>> graph;

    public MinCostMaxFlowSolver(int nodeCount) {
        this.graph = new ArrayList<>();

        for (int i = 0; i < nodeCount; i++) {
            graph.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int capacity, int cost) {
        CostFlowEdge forward = new CostFlowEdge(from, to, capacity, cost);
        CostFlowEdge backward = new CostFlowEdge(to, from, 0, -cost);

        forward.setReverse(backward);
        backward.setReverse(forward);

        graph.get(from).add(forward);
        graph.get(to).add(backward);
    }

    public int minCostMaxFlow(int source, int sink) {
        int totalFlow = 0;

        while (true) {
            int[] distance = new int[graph.size()];
            Arrays.fill(distance, Integer.MAX_VALUE);
            distance[source] = 0;

            CostFlowEdge[] parent = new CostFlowEdge[graph.size()];

            PriorityQueue<NodeDistance> queue = new PriorityQueue<>();
            queue.add(new NodeDistance(source, 0));

            while (!queue.isEmpty()) {
                NodeDistance current = queue.poll();

                if (current.distance() != distance[current.node()]) {
                    continue;
                }

                for (CostFlowEdge edge : graph.get(current.node())) {
                    if (edge.remainingCapacity() <= 0) {
                        continue;
                    }

                    int nextDistance = distance[current.node()] + edge.getCost();

                    if (nextDistance < distance[edge.getTo()]) {
                        distance[edge.getTo()] = nextDistance;
                        parent[edge.getTo()] = edge;
                        queue.add(new NodeDistance(edge.getTo(), nextDistance));
                    }
                }
            }

            if (parent[sink] == null) {
                break;
            }

            int pathFlow = Integer.MAX_VALUE;

            for (CostFlowEdge edge = parent[sink]; edge != null; edge = parent[edge.getFrom()]) {
                pathFlow = Math.min(pathFlow, edge.remainingCapacity());
            }

            for (CostFlowEdge edge = parent[sink]; edge != null; edge = parent[edge.getFrom()]) {
                edge.addFlow(pathFlow);
            }

            totalFlow += pathFlow;
        }

        return totalFlow;
    }

    public List<CostFlowEdge> getEdgesFrom(int node) {
        return graph.get(node);
    }

    private record NodeDistance(int node, int distance) implements Comparable<NodeDistance> {
        @Override
        public int compareTo(NodeDistance other) {
            return Integer.compare(this.distance, other.distance);
        }
    }
}