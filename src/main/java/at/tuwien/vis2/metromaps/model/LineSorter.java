package at.tuwien.vis2.metromaps.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LineSorter {

    static Logger logger = LoggerFactory.getLogger(LineSorter.class);

    public static List<Station> getOrderedStations(List<Station> stationsPerLine) {

        var unprocessedStations = new ArrayList<>(stationsPerLine);
        var currentStation =  unprocessedStations.get(0);
        LinkedList<Station> orderedStations = new LinkedList<>();
        orderedStations.add(currentStation);
        float threshold = 2f;
        while (!unprocessedStations.isEmpty()) {

            unprocessedStations.remove(currentStation);
            var nearestAfter = getNearestStationWithinThreshold(unprocessedStations, threshold, orderedStations.getLast().getCoordinates());
            if (nearestAfter != null) {
                orderedStations.addLast(nearestAfter);
                unprocessedStations.remove(nearestAfter);
            }
            var nearestBefore = getNearestStationWithinThreshold(unprocessedStations, threshold, orderedStations.getFirst().getCoordinates());
            if (nearestBefore != null) {
                orderedStations.addFirst(nearestBefore);
                unprocessedStations.remove(nearestBefore);
            }
            if (nearestBefore == null && nearestAfter == null) {
                logger.info(String.format("No match for %s found.", currentStation.getName()));
                orderedStations.add(currentStation);
                if (!unprocessedStations.isEmpty()) {
                    currentStation = unprocessedStations.get(0);
                }
            }
        }
        return orderedStations;
    }


    private static Station getNearestStationWithinThreshold(List<Station> stations, float threshold, double[] coordinatesRef) {

        double shortestDist = 1000;
        Station nearestStation = null;
        for (Station station : stations) {
            var distance = station.getDistanceInKmTo(coordinatesRef);
            if (distance < threshold && distance < shortestDist) {
                shortestDist = distance;
                nearestStation = station;
            }
        }
        return nearestStation;
    }
}
