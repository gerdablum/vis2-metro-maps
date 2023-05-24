package at.tuwien.vis2.metromaps.model;

public class Utils {

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
}
