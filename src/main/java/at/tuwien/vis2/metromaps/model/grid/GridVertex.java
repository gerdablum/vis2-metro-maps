package at.tuwien.vis2.metromaps.model.grid;

import at.tuwien.vis2.metromaps.model.input.InputLine;

import java.util.*;

/**
 * Represents a vertex on the grid graph. Each vertex is indexed and has coordinates.
 */
public class GridVertex {

    private String name;
    private int indexX;
    private int indexY;
    private double[] coordinates;
    private double[] labelCoordinates;
    private int labelRotation;
    private String stationName;
    private boolean isTaken = false;
    private Map<String, Boolean> takenLines;

    //used for dijkstra algortihm, represents the distance to current starting vertex
    private double distance;

    private LinkedList<GridVertex> shortestPath;


    /**
     * Creates a grid vertex with index and position.
     * @param name must be unique, mostly combination of x and y index
     * @param indexX relative x position in the grid
     * @param indexY relative y position in the grid
     * @param coordinates absolute positon of the grid.
     */
    public GridVertex(String name, int indexX, int indexY, double[] coordinates) {
        this.name = name;
        this.indexX = indexX;
        this.indexY = indexY;
        this.coordinates = coordinates;
        this.takenLines = new HashMap<>();
    }

    /**
     *
     * @return unique name of the grid
     */
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return relative x position in grid
     */
    int getIndexX() {
        return indexX;
    }

    void setIndexX(int indexX) {
        this.indexX = indexX;
    }

    /**
     * relative y position in grid
     * @return
     */

    public int getIndexY() {
        return indexY;
    }

    public void setIndexY(int indexY) {
        this.indexY = indexY;
    }

    void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    /**
     *
     * @return absolute position (coordinates) of vertex
     */
    public double[] getCoordinates() {
        return coordinates;
    }
    void setLabelCoordinates(double[] labelCoordinates) {
        this.labelCoordinates = labelCoordinates;
    }
    void setLabelRotation(int labelRotation) {
        this.labelRotation = labelRotation;
    }

    /**
     *
     * @return absolute proposed position of the labels
     */
    public double[] getLabelCoordinates() {
        return labelCoordinates;
    }

    /**
     *
     * @return proposed rotation of the labels
     */
    public int getLabelRotation() {
        return labelRotation;
    }

    /**
     *
     * @return name of the station this grid vertex is associated, null if it is not part of a sink edge or not routed (yet)
     */
    public String getStationName() {
        return stationName;
    }

    /**
     *
     * @return true if this vertex has already been routed, false otherwise
     */
    public boolean isTaken() {
        return isTaken;
    }

    /**
     * marks vertex as taken and sets the station name. This is used if the vertex is source or target of a routed path.
     * @param stationName name of the source/target station.
     */
    public void setTakenWith(String stationName) {
        isTaken = true;
        this.stationName = stationName;
    }

    public void setTaken() {
        isTaken = true;
    }

    public void release() {
        isTaken = false;
        this.stationName = null;
    }

    public Map<String, Boolean> getTakenLines() {
        return takenLines;
    }

    /**
     * marks a vertex as taken with the given line names
     * @param lineNames
     * @param lineName freshly routed line name
     */
    public void setTakenLineNames(List<InputLine> lineNames, String lineName) {
        for (InputLine line : lineNames) {
            this.takenLines.putIfAbsent(line.getName(), false);
        }
        this.takenLines.put(lineName, true);
    }

    double getDistance() {
        return distance;
    }

    void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GridVertex) {
            return this.getName().equals(((GridVertex) obj).getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    boolean isClosedForLine(String lineName) {
        Boolean taken = takenLines.get(lineName);
        if (taken == null) {
            return true;
        }
        return taken;
    }

    /**
     *
     * @return the shortest part from a source vertex to this vertex. Used when calculating dijskstra.
     */
    LinkedList<GridVertex> getShortestPath() {
        if (shortestPath == null) {
            return new LinkedList<>();
        }
        return shortestPath;
    }

    void setShortestPath(LinkedList<GridVertex> shortestPath) {
        this.shortestPath = shortestPath;
    }
}
