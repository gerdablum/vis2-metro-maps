package at.tuwien.vis2.metromaps.api;

import at.tuwien.vis2.metromaps.model.Edge;
import at.tuwien.vis2.metromaps.model.MetroDataProvider;
import at.tuwien.vis2.metromaps.model.Station;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class M10Service implements MetroDataProvider {


    Logger logger = LoggerFactory.getLogger(M10Service.class);
    private Resource data;

    private ObjectMapper objectMapper;
    private Map<String, Station> allStations;
    private Map<String, Edge> allEdges;

    @Autowired
    public M10Service(@Value("classpath:exports/UBAHNOGD_UBAHNHALTOGD.json") Resource data) {
        allStations = new HashMap<>();
        allEdges = new HashMap<>();
        objectMapper = new ObjectMapper();
        this.data = data;
        parseData();
    }

    private void parseData() {
        //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            M10Features subwayStations = objectMapper.readValue(data.getFile(), M10Features.class);
            for(M10Features.Feature feature : subwayStations.getFeatures()) {
                if("Point".equals(feature.getType())) {
                    Station station = new Station(feature.getStationName(), feature.getId(), feature.getCoordinates()[0],
                            Arrays.asList(String.valueOf(feature.getLineName())));
                    allStations.put(station.getName(), station);
                }
                else if("LineString".equals(feature.getType())) {
                    Edge edge = new Edge(feature.getId(), feature.getCoordinates(), Collections.singletonList(String.valueOf(feature.getLineName())));
                    allEdges.put(edge.getId(), edge);
                }
                else {
                    logger.warn(String.format("Warning: %s with type %s cannot be assigned",
                            feature.getId(), feature.getType()));
                }
            }
        } catch (IOException e) {
            logger.error("Error reading vienna json file", e);
        }
    }
    @Override
    public List<Station> getAllStations() {
        return allStations.values().stream().toList();
    }

    @Override
    public List<Edge> getAllGeograficEdges() {
        return allEdges.values().stream().toList();
    }

    @Override
    public List<Station> getAllStationsForLine(String lineId) {
        return getAllStations().stream()
                .filter(station -> station.getLineNames().contains(lineId)).toList();
    }

    @Override
    public List<Edge> getAllGeograficLineEdges(String lineId) {
        return allEdges.values().stream()
                .filter(edge -> edge.getLineNames().contains(lineId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Station> getOrderedStationsForLine(String lineId) {
        var allStationsForLine = getAllStationsForLine(lineId);
        return getOrderedStations(allStationsForLine);
    }

    @Override
    public List<Edge> getOrderedEdgesForLine(String lineId) {
        var orderedStations = getOrderedStationsForLine(lineId);
        var orderedEdges = new ArrayList<Edge>();
        for (int i = 0; i < orderedStations.size() -1; i++) {
            var currentStation = orderedStations.get(i);
            var nextStation = orderedStations.get(i+1);
            Edge edge = new Edge(currentStation.getId()+"+"+nextStation.getId(), currentStation, nextStation,
                    new double[][]{currentStation.getCoordinates(), nextStation.getCoordinates()}, Collections.singletonList(lineId));
            orderedEdges.add(edge);
        }
        return orderedEdges;
    }

    private List<Station> getOrderedStations(List<Station> stationsPerLine) {

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

    private Station getNearestStationWithinThreshold(List<Station> stations, float threshold, double[] coordinatesRef) {

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
