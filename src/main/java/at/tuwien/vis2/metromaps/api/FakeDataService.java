package at.tuwien.vis2.metromaps.api;

import at.tuwien.vis2.metromaps.model.MetroDataProvider;
import at.tuwien.vis2.metromaps.model.MetroLineEdge;
import at.tuwien.vis2.metromaps.model.Station;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FakeDataService implements MetroDataProvider {

    private List<Station> stations;
    private List<MetroLineEdge> edges;

    public FakeDataService() {
        Station ottakring = new Station("Ottakring", "1", new double[]{48.211059149435854,16.311366713192395}, Collections.singletonList("3"));
        Station kendlerstrasse =  new Station("Kendlerstraße", "2", new double[]{48.204540181864424,16.309147428955267}, Collections.singletonList("3"));
        Station huettldorferstr =  new Station("Hütteldorfer Straße", "3", new double[]{48.19979659129259,16.311393949424332}, Collections.singletonList("3"));
        Station leopoldau =  new Station("Leopoldau", "4", new double[]{48.27751786569251,16.452139552735247}, Collections.singletonList("1"));
        Station grossfelds =  new Station("Großfeldsiedlung", "5", new double[]{48.27101076563699,16.447882377130643}, Collections.singletonList("1"));
        Station aderklaaer =  new Station("Aderklaaer Straße", "6", new double[]{48.26342048389046,16.45162591874024}, Collections.singletonList("1"));
        MetroLineEdge edge1 = new MetroLineEdge("1", ottakring, kendlerstrasse, new double[1][0], Collections.singletonList("3"));
        MetroLineEdge edge2 = new MetroLineEdge("1", kendlerstrasse, huettldorferstr, new double[1][0], Collections.singletonList("3"));
        MetroLineEdge edge3 = new MetroLineEdge("3", leopoldau, grossfelds, new double[1][0], Collections.singletonList("3"));
        MetroLineEdge edge4 = new MetroLineEdge("4", grossfelds, aderklaaer, new double[1][0], Collections.singletonList("3"));

        this.stations = Arrays.asList(ottakring, kendlerstrasse, huettldorferstr, leopoldau, aderklaaer, grossfelds);
        this.edges = Arrays.asList(edge1, edge2, edge3, edge4);
    }

    @Override
    public List<Station> getAllStations() {
        return stations;
    }

    @Override
    public List<MetroLineEdge> getAllGeograficEdges() {
        return this.edges;
    }

    @Override
    public List<Station> getAllStationsForLine(String lineId) {
        return this.stations;
    }

    @Override
    public List<MetroLineEdge> getEdgesWithoutStationInformation(String lineId) {
        return this.edges;
    }

    @Override
    public List<Station> getOrderedStationsForLine(String lineId) {
        return this.stations;
    }

    @Override
    public List<MetroLineEdge> getOrderedEdgesForLine(String lineId) {
        return this.edges;
    }
}
