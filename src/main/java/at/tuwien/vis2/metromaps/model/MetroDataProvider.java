package at.tuwien.vis2.metromaps.model;

import java.util.List;

public interface MetroDataProvider {

    List<Station> getAllStations();

    List<MetroLineEdge> getAllGeograficEdges();

    List<Station> getAllStationsForLine(String lineId);

    List<MetroLineEdge> getEdgesWithoutStationInformation(String lineId);

    List<Station> getOrderedStationsForLine(String lineId);

    List<MetroLineEdge> getOrderedEdgesForLine(String lineId);
}
