package at.tuwien.vis2.metromaps.api;

import at.tuwien.vis2.metromaps.model.MetroDataProvider;
import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FakeDataService implements MetroDataProvider {

    private List<InputStation> stations;
    private List<InputLineEdge> edges;

    public FakeDataService() {
        InputStation ottakring = new InputStation("Ottakring", "1", new double[]{48.211059149435854,16.311366713192395}, Collections.singletonList("3"));
        InputStation kendlerstrasse =  new InputStation("Kendlerstraße", "2", new double[]{48.204540181864424,16.309147428955267}, Collections.singletonList("3"));
        InputStation huettldorferstr =  new InputStation("Hütteldorfer Straße", "3", new double[]{48.19979659129259,16.311393949424332}, Collections.singletonList("3"));
        InputStation leopoldau =  new InputStation("Leopoldau", "4", new double[]{48.27751786569251,16.452139552735247}, Collections.singletonList("1"));
        InputStation grossfelds =  new InputStation("Großfeldsiedlung", "5", new double[]{48.27101076563699,16.447882377130643}, Collections.singletonList("1"));
        InputStation aderklaaer =  new InputStation("Aderklaaer Straße", "6", new double[]{48.26342048389046,16.45162591874024}, Collections.singletonList("1"));
        InputLineEdge edge1 = new InputLineEdge("1", ottakring, kendlerstrasse, new double[1][0], Collections.singletonList("3"));
        InputLineEdge edge2 = new InputLineEdge("1", kendlerstrasse, huettldorferstr, new double[1][0], Collections.singletonList("3"));
        InputLineEdge edge3 = new InputLineEdge("3", leopoldau, grossfelds, new double[1][0], Collections.singletonList("3"));
        InputLineEdge edge4 = new InputLineEdge("4", grossfelds, aderklaaer, new double[1][0], Collections.singletonList("3"));

        this.stations = Arrays.asList(ottakring, kendlerstrasse, huettldorferstr);
        this.edges = Arrays.asList(edge1, edge2);
    }

    @Override
    public List<InputStation> getAllStations(String city) {
        return stations;
    }

    @Override
    public List<InputLineEdge> getAllGeograficEdges(String city) {
        return this.edges;
    }


    @Override
    public List<InputLineEdge> getAllGeograficEdgesForLine(String lineId, String city) {
        return this.edges;
    }

    @Override
    public List<InputLineEdge> getOrderedEdgesForLine(String lineId, String city) {
        return this.edges;
    }

    @Override
    public List<String> getAllLineNames(String city) {
        return Arrays.asList("1");
    }
}
