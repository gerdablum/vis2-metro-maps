package at.tuwien.vis2.metromaps.model;

public class Edge {

    private String id;
    private Station startStation;
    private Station endStation;

    private double[][] coordinates;
    private String lineName;

    public Edge(String id, Station startStation, Station endStation, double[][] coordinates, String lineName) {
        this.id = id;
        this.startStation = startStation;
        this.endStation = endStation;
        this.coordinates = coordinates;
        this.lineName = lineName;
    }

    public Edge(String id, double[][] coordinates, String lineName) {
        this.id = id;
        this.coordinates = coordinates;
        this.lineName = lineName;
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

    public String getLineName() {
        return lineName;
    }

    public double[][] getCoordinates() {
        return coordinates;
    }
}
