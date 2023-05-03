package at.tuwien.vis2.metromaps.model;

import java.util.HashSet;
import java.util.List;

public class MetroLineEdge {

    private String id;
    private Station startStation;
    private Station endStation;

    // TODO currently only needed to draw coordinates in map. We should distinguish between edges for graph and
    // geographical lines for drawing with different datatypes.
    private double[][] coordinates;
    private List<String> lineNames;

    public MetroLineEdge(String id, Station startStation, Station endStation, double[][] coordinates, List<String> lineNames) {
        this.id = id;
        this.startStation = startStation;
        this.endStation = endStation;
        this.coordinates = coordinates;
        this.lineNames = lineNames;
    }

    public MetroLineEdge(String id, double[][] coordinates, List<String> lineNames) {
        this.id = id;
        this.coordinates = coordinates;
        this.lineNames = lineNames;
    }

    public String getId() {
        return id;
    }

    public Station getStartStation() {
        return startStation;
    }

    public void setStartStation(Station startStation) {
        this.startStation = startStation;
    }

    public Station getEndStation() {
        return endStation;
    }

    public void setEndStation(Station endStation) {
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
        if (obj instanceof MetroLineEdge) {
            boolean hasSameLineEdges = new HashSet<>(this.getLineNames()).containsAll(((MetroLineEdge) obj).getLineNames());
            // TODO also check for equality for start == end && end == start
            return hasSameLineEdges && (this.startStation.equals(((MetroLineEdge) obj).getStartStation()) && this.endStation.equals(((MetroLineEdge) obj).getEndStation()));
        }
        return false;
    }
}