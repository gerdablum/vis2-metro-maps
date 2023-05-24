package at.tuwien.vis2.metromaps.model.grid;

import java.util.Objects;

// TODO this class is to wrap default edge and prevent serializing errors with jackson
public class GridEdge {


    private GridVertex source;
    private GridVertex destination;
    private int bendCost;

    public GridEdge(GridVertex source, GridVertex destination, int bendCost ) {
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

    public int getBendCost() {
        return bendCost;
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
