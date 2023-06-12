package at.tuwien.vis2.metromaps.model.grid;

import at.tuwien.vis2.metromaps.model.input.InputLine;

import java.util.*;

public class GridVertex {

    private String name;
    private int indexX;
    private int indexY;
    private double[] coordinates;
    private double[] labelCoordinates;
    private String stationName;
    private boolean isTaken = false;
    private Map<String, Boolean> takenLines;

    //used for dijkstra algortihm, represents the distance to current starting vertex
    private double distance;

    private LinkedList<GridVertex> shortestPath;


    public GridVertex(String name, int indexX, int indexY, double[] coordinates) {
        this.name = name;
        this.indexX = indexX;
        this.indexY = indexY;
        this.coordinates = coordinates;
        this.takenLines = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndexX() {
        return indexX;
    }

    public void setIndexX(int indexX) {
        this.indexX = indexX;
    }

    public int getIndexY() {
        return indexY;
    }

    public void setIndexY(int indexY) {
        this.indexY = indexY;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public double[] getCoordinates() {
        return coordinates;
    }
    public void setLabelCoordinates(double[] labelCoordinates) {
        this.labelCoordinates = labelCoordinates;
    }

    public double[] getLabelCoordinates() {
        return labelCoordinates;
    }
    public String getStationName() {
        return stationName;
    }

    public boolean isTaken() {
        return isTaken;
    }

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

    public boolean isClosedForLine(String lineName) {
        Boolean taken = takenLines.get(lineName);
        if (taken == null) {
            return true;
        }
        return taken;
    }

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
