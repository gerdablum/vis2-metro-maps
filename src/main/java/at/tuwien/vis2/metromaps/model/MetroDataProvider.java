package at.tuwien.vis2.metromaps.model;

import java.util.List;

public interface MetroDataProvider {

    List<Station> getAllStations();

    List<Edge> getAllEdges();

    List<Station> getAllStationsForLine(String lineName);

    List<Edge> getLine(String lineName);

    Station getStationById(String id);


    List<Edge> getAllEdgesForLine(String lineId);
}
