package at.tuwien.vis2.metromaps.model;

import java.util.List;

public interface MetroDataProvider {

    List<Station> getAllStations();

    List<Edge> getAllGeograficEdges();

    List<Station> getAllStationsForLine(String lineId);

    List<Edge> getEdgesWithoutStationInformation(String lineId);

    List<Station> getOrderedStationsForLine(String lineId);

    List<Edge> getOrderedEdgesForLine(String lineId);
}
