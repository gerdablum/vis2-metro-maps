package at.tuwien.vis2.metromaps.model.input;

import at.tuwien.vis2.metromaps.model.grid.GridVertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class InputLineEdge {

    private String id;
    private InputStation startStation;
    private InputStation endStation;

    // TODO currently only needed to draw coordinates in map. We should distinguish between edges for graph and
    // geographical lines for drawing with different datatypes.
    private double[][] coordinates;
    private List<String> lineNames;

    public InputLineEdge(String id, InputStation startStation, InputStation endStation, double[][] coordinates, List<String> lineNames) {
        this.id = id;
        this.startStation = startStation;
        this.endStation = endStation;
        this.coordinates = coordinates;
        this.lineNames = new ArrayList<>(lineNames);
    }

    public InputLineEdge(String id, double[][] coordinates, List<String> lineNames) {
        this.id = id;
        this.coordinates = coordinates;
        this.lineNames = new ArrayList<>(lineNames);
    }

    public String getId() {
        return id;
    }

    public InputStation getStartStation() {
        return startStation;
    }

    public void setStartStation(InputStation startStation) {
        this.startStation = startStation;
    }

    public InputStation getEndStation() {
        return endStation;
    }

    public void setEndStation(InputStation endStation) {
        this.endStation = endStation;
    }

    public List<String> getLineNames() {
        return lineNames;
    }

    public double[][] getCoordinates() {
        return coordinates;
    }

    @Override
    public int hashCode() {
        return lineNames.hashCode() + startStation.hashCode() + endStation.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InputLineEdge) {
            boolean hasSameLineEdges = new HashSet<>(this.getLineNames()).containsAll(((InputLineEdge) obj).getLineNames());
            // TODO also check for equality for start == end && end == start??
            return hasSameLineEdges && (this.startStation.equals(((InputLineEdge) obj).getStartStation()) && this.endStation.equals(((InputLineEdge) obj).getEndStation()));
        }
        return false;
    }

    public void reverse() {
        InputStation temp = this.endStation;
        this.endStation = this.startStation;
        this.startStation = temp;
    }

    public void addLineName(String lineId) {
        if (!lineNames.contains(lineId)) {
            lineNames.add(lineId);
        }
    }
}
