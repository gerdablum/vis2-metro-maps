package at.tuwien.vis2.metromaps.model;

import java.util.List;

public class Station {

    private String name;
    private String id;
    private double[] coordinates;
    private List<String> lineNames;

    public Station(String name, String id, double[] coordinates, List<String> lineNames) {
        this.name = name;
        this.id = id;
        this.coordinates = coordinates;
        this.lineNames = lineNames;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public List<String> getLineNames() {
        return lineNames;
    }


}
