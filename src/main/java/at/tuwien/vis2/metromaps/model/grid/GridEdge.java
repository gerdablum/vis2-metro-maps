package at.tuwien.vis2.metromaps.model.grid;

import java.util.*;

// TODO this class is to wrap default edge and prevent serializing errors with jackson
public class GridEdge {

    public boolean isClosedForLine(String lineName) {
        Boolean isLineTaken = this.takenLines.get(lineName);
        if (isLineTaken == null) {
            return true;
        }

        return isLineTaken;
    }

    public enum BendCost {
        C_45(2),
        C_90(1.5f),
        C_135(1),
        C_180(0);
        private float weight;
        private BendCost(float weight) {
            this.weight = weight;
        }

        public float getWeight() {
            return this.weight;
        }
    }

    private GridVertex source;
    private GridVertex destination;
    private BendCost bendCost;
    private double offsetCosts = 1;
    private double costs = 1;

    private Map<String, Boolean> takenLines;

    public GridEdge(GridVertex source, GridVertex destination, BendCost bendCost ) {
        this.source = source;
        this.destination = destination;
        this.bendCost = bendCost;
        takenLines = new HashMap<>();
    }

    public GridVertex getSource() {
        return source;
    }

    public GridVertex getDestination() {
        return destination;
    }

    public BendCost getBendCost() {
        return bendCost;
    }

    public double updateCosts(double offsetCosts) {
        if (costs == Double.MAX_VALUE) {
            return costs;
        }
        this.offsetCosts = offsetCosts;
        costs = this.bendCost.getWeight() + offsetCosts;
        return costs;
    }

    public void setTaken(List<String> lines, String lineName) {
        for (String line : lines) {
            if (takenLines.get(line) == null) {
                takenLines.put(line, line.equals(lineName));
            }
        }
        takenLines.put(lineName, true);
    }

    public void setCostsInf() {
        costs = Double.MAX_VALUE;
    }

    public void resetCosts() {
        costs = 1;
        bendCost = BendCost.C_180;
    }

    public double updateCosts(BendCost newBendCost) {
        if (costs == Double.MAX_VALUE) {
            return costs;
        }
        this.bendCost = newBendCost;
        costs = this.bendCost.getWeight() + offsetCosts;
        return costs;
    }

    public double getCosts() {
        return costs;
    }

    public void reverse() {
        GridVertex temp = this.destination;
        this.destination = this.source;
        this.source = temp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridEdge gridEdge = (GridEdge) o;
        return Objects.equals(source, gridEdge.source) && Objects.equals(destination, gridEdge.destination) ||
                Objects.equals(source, gridEdge.destination) && Objects.equals(destination, gridEdge.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination) + Objects.hash(destination, source);
    }

    @Override
    public String toString() {
        return "GridEdge{" +
                "source=" + source +
                ", destination=" + destination +
                '}';
    }
}
