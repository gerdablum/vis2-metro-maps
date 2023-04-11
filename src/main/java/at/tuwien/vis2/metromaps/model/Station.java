package at.tuwien.vis2.metromaps.model;

import java.util.List;

public class Station extends Vertex{

    // actually the name is currently primary key, id is not really necessary at this point
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

    public double getDistanceInKmTo(double[] coordinatesRef) {
        double latRefRadian = Math.toRadians(coordinatesRef[0]);
        double lonRefRadian = Math.toRadians(coordinatesRef[1]);
        double latRadian = Math.toRadians(coordinates[0]);
        double lonRadian = Math.toRadians(coordinates[1]);
        double dlon = lonRadian - lonRefRadian;
        double dlat = latRadian - latRefRadian;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(latRefRadian) * Math.cos(latRadian)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));
        // Radius of earth in kilometers
        double r = 6371;
        return c * r;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Station) {
            return this.getName().equals(((Station) obj).getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.getId() + ")";
    }
}
