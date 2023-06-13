package at.tuwien.vis2.metromaps.model.grid;

import at.tuwien.vis2.metromaps.model.input.InputLine;

import java.util.*;

public class GridEdge {

    public boolean isClosedForLine(String lineName) {
        // TODO what does this method actually do??
        Boolean isLineTaken = this.takenLines.get(lineName);
        if (isLineTaken == null) {
            return true;
        }
        return isLineTaken;
    }


    public boolean isAlreadyRoutedForLine(String lineName) {
        Boolean isLineTaken = this.takenLines.get(lineName);
        if (isLineTaken == null) {
            return false;
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

    private List<String> colors;

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

    public void updateCosts(double offsetCosts) {
        if (costs == Double.MAX_VALUE) {
            return;
        }
        this.offsetCosts = offsetCosts;
        costs = this.bendCost.getWeight() + offsetCosts;
    }

    public void setTaken(List<InputLine> lines, String lineName) {
        for (InputLine line : lines) {
            if (takenLines.get(line.getName()) == null) {
                takenLines.put(line.getName(), line.getName().equals(lineName));
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

    public void updateCosts(BendCost newBendCost) {
        if (costs == Double.MAX_VALUE) {
            return;
        }
        this.bendCost = newBendCost;
        costs = this.bendCost.getWeight() + offsetCosts;
    }

    public double getCosts() {
        return costs;
    }

    public void reverse() {
        GridVertex temp = this.destination;
        this.destination = this.source;
        this.source = temp;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        if (this.colors == null) {
            this.colors = new ArrayList<>(colors);
        } else {
            for (String color : colors) {
                if (!this.colors.contains(color)) {
                    this.colors.add(color);
                }
            }
        }
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
