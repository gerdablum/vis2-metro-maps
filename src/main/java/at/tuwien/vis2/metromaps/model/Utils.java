package at.tuwien.vis2.metromaps.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static final String vienna = "Vienna";
    public static final String berlin = "Berlin";
    public static final String freiburg = "Freiburg";
    public static final String london = "London";
    public static final String nyc = "New York";

    public static final List<String> allCities = Arrays.asList(vienna, berlin, freiburg, london, nyc);


    public static double getDistanceInKmTo(double[] coordinatesRef, double[] coordinates) {
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
        double r = 6378.1;
        return c * r;
    }

    public static double[] convertEspg3857ToLatLon(double x, double y) {
        x = (x * 180) / 20037508.34;
        y = (y * 180) / 20037508.34;

        y = (Math.atan(Math.pow(Math.E, y * (Math.PI / 180))) * 360) / Math.PI - 90;
        return new double[] {x, y};
    }
}
