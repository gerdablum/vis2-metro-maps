package at.tuwien.vis2.metromaps.model.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridVertex {

    private String name;
    private int indexX;
    private int indexY;
    private double[] coordinates;
    private String stationName;
    private boolean isTaken = false;
    private Map<String, Boolean> takenLines;


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

    public double[] getCoordinates() {
        return coordinates;
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

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }


    public Map<String, Boolean> getTakenLines() {
        return takenLines;
    }

    public void setTakenLineNames(List<String> lineNames, String lineName) {
        for (String line : lineNames) {
            this.takenLines.putIfAbsent(line, false);
        }
        this.takenLines.put(lineName, true);
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
}
