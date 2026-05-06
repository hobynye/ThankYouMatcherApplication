package org.hobynye.thankyoumatcher.solver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CostFlowEdge {

    private final int from;
    private final int to;
    private final int capacity;
    private final int cost;

    private int flow;
    private CostFlowEdge reverse;

    public CostFlowEdge(int from, int to, int capacity, int cost) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.cost = cost;
        this.flow = 0;
    }

    public int remainingCapacity() {
        return capacity - flow;
    }

    public void addFlow(int amount) {
        this.flow += amount;
        this.reverse.flow -= amount;
    }
}