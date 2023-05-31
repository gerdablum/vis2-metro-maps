package at.tuwien.vis2.metromaps.model.grid;

import java.util.Objects;

// TODO this class is to wrap default edge and prevent serializing errors with jackson
public class GridEdge {

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

    public GridEdge(GridVertex source, GridVertex destination, BendCost bendCost ) {
        this.source = source;
        this.destination = destination;
        this.bendCost = bendCost;
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
        this.offsetCosts = offsetCosts;
        costs = this.bendCost.getWeight() + offsetCosts;
    }

    public void setCostsInf() {
        costs = Double.MAX_VALUE;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridEdge gridEdge = (GridEdge) o;
        return Objects.equals(source, gridEdge.source) && Objects.equals(destination, gridEdge.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }

    @Override
    public String toString() {
        return "GridEdge{" +
                "source=" + source +
                ", destination=" + destination +
                '}';
    }
}
