package at.tuwien.vis2.metromaps.model.grid;

import at.tuwien.vis2.metromaps.model.input.InputLine;

import java.util.*;

/**
 * This class reprents an edge on the grid graph
 */
public class GridEdge {

    /**
     * checks if an edge is closed for this particular line
     * @param lineName
     * @return true is the line is closed, false otherwise
     */
    public boolean isClosedForLine(String lineName) {
        Boolean isLineTaken = this.takenLines.get(lineName);
        if (isLineTaken == null) {
            return true;
        }
        return isLineTaken;
    }

    /**
     * represents bend costs as stated in the paper
     */
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

    /**
     * Creates a grid edge with from source to destination with initial bend cost
     * @param source GridVertex source
     * @param destination GridVertex destination
     * @param bendCost
     */
    public GridEdge(GridVertex source, GridVertex destination, BendCost bendCost ) {
        this.source = source;
        this.destination = destination;
        this.bendCost = bendCost;
        takenLines = new HashMap<>();
    }

    /**
     *
     * @return adjacent grid vertex
     */
    public GridVertex getSource() {
        return source;
    }

    /**
     *
     * @return adjacent grid vertex
     */
    public GridVertex getDestination() {
        return destination;
    }

    public BendCost getBendCost() {
        return bendCost;
    }

    /**
     * Changes the offset costs and recalculates the total cost.
     * Note: Already infinite costs stay infinite.
     * @param offsetCosts costs depending on how far the grid edge is away from the original station where the path
     *                    is routed.
     */
    public void updateCosts(double offsetCosts) {
        if (costs == Double.MAX_VALUE) {
            return;
        }
        this.offsetCosts = offsetCosts;
        costs = this.bendCost.getWeight() + offsetCosts;
    }

    /**
     * marks the edge taken for a specific lines. This method is used if a path has been routed through this edge.
     * @param lines all lines of an input edge. They are added to the gridEdge and marked as "not taken"
     * @param lineName line name of the routed edge. This line is marked as taken in the gridEdge
     */
    public void setTaken(List<InputLine> lines, String lineName) {
        for (InputLine line : lines) {
            if (takenLines.get(line.getName()) == null) {
                takenLines.put(line.getName(), line.getName().equals(lineName));
            }
        }
        takenLines.put(lineName, true);
    }

    /**
     * Sets the total costs of a grid edge to inf, which means it is considered to be closed.
     */
    public void setCostsInf() {
        costs = Double.MAX_VALUE;
    }

    /**
     * Sets the offset costs to 1 and bend costs to 180 (0)
     */
    public void resetCosts() {
        costs = 1;
        bendCost = BendCost.C_180;
    }

    /**
     * Updates bend costs and adds them to the total costs. Not that already closed edges will still have costs inf.
     * @param newBendCost freshly calculated bend costs to update
     */
    public void updateCosts(BendCost newBendCost) {
        if (costs == Double.MAX_VALUE) {
            return;
        }
        this.bendCost = newBendCost;
        costs = this.bendCost.getWeight() + offsetCosts;
    }

    /**
     *  total costs of a grid edge.
     * @return 1 if neither bendcost nor offset cost have been calculated. total costs otherwise.
     */
    public double getCosts() {
        return costs;
    }

    /**
     * swaps source and destination of a grid edge
     */
    public void reverse() {
        GridVertex temp = this.destination;
        this.destination = this.source;
        this.source = temp;
    }

    /**
     * used for serializing.
     * @return list of colors if the edge has a routed path, null otherwise.
     */
    public List<String> getColors() {
        return colors;
    }

    /**
     * if a path is routed with a color or a set of colors from an input line, these colors are added to a list, such that
     * the frontend can display them.
     * @param colors list of colors from a freshly routed path.
     */
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
