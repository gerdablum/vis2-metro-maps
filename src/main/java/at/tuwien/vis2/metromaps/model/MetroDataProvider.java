package at.tuwien.vis2.metromaps.model;

import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;

import java.util.List;

public interface MetroDataProvider {

    List<InputStation> getAllStations(String city);

    List<InputLineEdge> getAllGeograficEdges(String city);

    List<InputLineEdge> getAllGeograficEdgesForLine(String lineId, String city);

    List<InputLineEdge> getOrderedEdgesForLine(String lineId, String city);

    List<String> getAllLineNames(String city);
}
