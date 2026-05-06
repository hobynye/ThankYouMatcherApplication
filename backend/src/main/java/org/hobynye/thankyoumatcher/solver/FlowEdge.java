package org.hobynye.thankyoumatcher.solver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowEdge {

    private final int from;
    private final int to;
    private final int capacity;

    private int flow;
    private FlowEdge reverse;

    public FlowEdge(int from, int to, int capacity) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
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