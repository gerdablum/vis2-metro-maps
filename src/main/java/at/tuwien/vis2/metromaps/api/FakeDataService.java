package at.tuwien.vis2.metromaps.api;

import at.tuwien.vis2.metromaps.model.MetroDataProvider;
import at.tuwien.vis2.metromaps.model.MetroLineEdge;
import at.tuwien.vis2.metromaps.model.Station;

import java.util.List;

public class FakeDataService implements MetroDataProvider {
    @Override
    public List<Station> getAllStations() {
        return null;
    }

    @Override
    public List<MetroLineEdge> getAllGeograficEdges() {
        return null;
    }

    @Override
    public List<Station> getAllStationsForLine(String lineId) {
        return null;
    }

    @Override
    public List<MetroLineEdge> getEdgesWithoutStationInformation(String lineId) {
        return null;
    }

    @Override
    public List<Station> getOrderedStationsForLine(String lineId) {
        return null;
    }

    @Override
    public List<MetroLineEdge> getOrderedEdgesForLine(String lineId) {
        return null;
    }
}
