package at.tuwien.vis2.metromaps.model;

import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;

import java.util.List;

public interface MetroDataProvider {

    List<InputStation> getAllStations();

    List<InputLineEdge> getAllGeograficEdges();

    List<InputStation> getAllStationsForLine(String lineId);

    List<InputLineEdge> getEdgesWithoutStationInformation(String lineId);

    List<InputStation> getOrderedStationsForLine(String lineId);

    List<InputLineEdge> getOrderedEdgesForLine(String lineId);
}
