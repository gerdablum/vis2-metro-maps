package at.tuwien.vis2.metromaps.api.paper;

import at.tuwien.vis2.metromaps.model.MetroDataProvider;
import at.tuwien.vis2.metromaps.model.Utils;
import at.tuwien.vis2.metromaps.model.input.InputLine;
import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaperMetroDataService implements MetroDataProvider {

    Logger logger = LoggerFactory.getLogger(PaperMetroDataService.class);

    private HashMap<String, ResourceWrapper> resources;
    private ObjectMapper objectMapper;

    @Autowired
    public PaperMetroDataService() {
        Resource berlin = new ClassPathResource("exports/berlin.json");
        Resource freiburg = new ClassPathResource("exports/freiburg.json");
        Resource london = new ClassPathResource("exports/london.json");
        Resource nyc = new ClassPathResource("exports/nyc_subway.json");
        Resource vienna = new ClassPathResource("exports/wien.json");

        this.resources = new HashMap<>();
        resources.put(Utils.berlin, new ResourceWrapper(berlin));
        resources.put(Utils.freiburg, new ResourceWrapper(freiburg));
        resources.put(Utils.london, new ResourceWrapper(london));
        resources.put(Utils.nyc, new ResourceWrapper(nyc));
        resources.put(Utils.vienna, new ResourceWrapper(vienna));

        this.objectMapper = new ObjectMapper();
        loadData();
    }

    private void loadData() {
        for (Map.Entry<String, ResourceWrapper> entry : resources.entrySet()) {
            ResourceWrapper wrapper = entry.getValue();
            try {
                PaperFeatures subwayStations = objectMapper.readValue(wrapper.resource.getFile(), PaperFeatures.class);
                List<PaperFeatures.Feature> allPoints = Arrays.stream(subwayStations.getFeatures()).filter( a -> "Point".equals(a.getType())).toList();
                List<PaperFeatures.Feature> allLines = Arrays.stream(subwayStations.getFeatures()).filter( a -> "LineString".equals(a.getType())).toList();
                wrapper.allPoints = allPoints;
                wrapper.allLines = allLines;

                wrapper.allInputStations = new HashMap<>();
                for (PaperFeatures.Feature point : wrapper.allPoints) {
                    wrapper.allInputStations.put(point.getProperties().getId(),new InputStation(point.getProperties().getStationLabel(),
                            point.getProperties().getId(), point.getCoordinates()[0], null));
                }

                wrapper.allInputLineEdges = new HashMap<>();
                for (PaperFeatures.Feature line : wrapper.allLines) {
                    List<PaperFeatures.Line> lines = line.getProperties().getLines();
                    List<InputLine> inputLine = lines.stream().map(l -> new InputLine(l.getLabel(), l.getId())).collect(Collectors.toList());
                    String from = line.getProperties().getFrom();
                    String to = line.getProperties().getTo();
                    wrapper.allInputStations.get(from).addLines(inputLine);
                    wrapper.allInputStations.get(to).addLines(inputLine);
                    InputStation startStation = wrapper.allInputStations.get(from);
                    InputStation endStations = wrapper.allInputStations.get(to);
                    InputLineEdge lineEdge = new InputLineEdge(line.getProperties().getId(), startStation, endStations,
                            line.getCoordinates(), inputLine);
                    wrapper.allInputLineEdges.put(lineEdge.getId(), lineEdge);
                }

            } catch (IOException e) {
                logger.error(String.format("importing of %s threw an exception:", wrapper.resource.getFilename()));
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public List<InputStation> getAllStations(String city) {
        ResourceWrapper wrapper = resources.get(city);
        Collection<InputStation> stations = wrapper.allInputStations.values();
        List<InputStation> stationList = new ArrayList<>();
        stationList.addAll(stations);
        return stationList;
    }

    @Override
    public List<InputLineEdge> getAllGeograficEdges(String city) {
        ResourceWrapper wrapper = resources.get(city);
        Collection<InputLineEdge> values = wrapper.allInputLineEdges.values();
        List<InputLineEdge> allEdges = new ArrayList<>();
        allEdges.addAll(values);
        return allEdges;
    }

    @Override
    public List<InputLineEdge> getAllGeograficEdgesForLine(String lineId, String city) {
        ResourceWrapper wrapper = resources.get(city);
        Collection<InputLineEdge> lineEdges = wrapper.allInputLineEdges.values();
        List<InputLineEdge> outputList = new ArrayList<>();
        for (InputLineEdge edge:  lineEdges) {
            for (InputLine lineNames : edge.getLines())
                if (lineNames.getName().contains(lineId)) {
                    outputList.add(edge);
                }
        }
        return outputList;
    }

    @Override
    public List<String> getAllLineNames(String city) {
        List<InputLineEdge> allGeograficEdges = getAllGeograficEdges(city);
        Set<String> lineNames = new HashSet<>();
        for (InputLineEdge edge: allGeograficEdges) {
            lineNames.addAll(edge.getLines().stream().map(InputLine::getName).toList());
        }
        return lineNames.stream().toList();
    }

    @Override
    public List<InputLineEdge> getOrderedEdgesForLine(String lineId, String city) {
        List<InputLineEdge> allGeograficEdgesForLine = getAllGeograficEdgesForLine(lineId, city);
        LinkedList<InputLineEdge> orderedEdges = new LinkedList<>();
        while (!allGeograficEdgesForLine.isEmpty()) {
            LinkedList<InputLineEdge> semiOrderedEdges = new LinkedList<>();
            orderEdges(allGeograficEdgesForLine, semiOrderedEdges, lineId);
            orderedEdges.addAll(semiOrderedEdges);

        }

        return orderedEdges;
    }

    private void orderEdges(List<InputLineEdge> allGeograficEdgesForLine, LinkedList<InputLineEdge> orderedEdges, String lineId) {
        InputLineEdge current = allGeograficEdgesForLine.get(0);
        while (current != null) {
            orderedEdges.addLast(current);
            allGeograficEdgesForLine.remove(current);
            InputStation nextStation = current.getEndStation();
            current = searchForFollowingLines(allGeograficEdgesForLine, nextStation);
        }
        InputLineEdge firstStation = orderedEdges.getFirst();
        InputStation next = firstStation.getStartStation();
        current = searchForPreviousLines(allGeograficEdgesForLine, next);
        while (current != null) {
            orderedEdges.addFirst(current);
            allGeograficEdgesForLine.remove(current);
            InputStation nextStation = current.getStartStation();
            current = searchForPreviousLines(allGeograficEdgesForLine, nextStation);
        }
    }

    private InputLineEdge searchForFollowingLines(List<InputLineEdge> edges, InputStation endStation) {
        for (InputLineEdge edge : edges) {
            if (edge.getStartStation().equals(endStation)) {
                return edge;
            } else if (edge.getEndStation().equals(endStation)) {
                edge.reverse();
                return edge;
            }
        }
        return null;
    }

    private InputLineEdge searchForPreviousLines(List<InputLineEdge> edges, InputStation startStation) {
        for (InputLineEdge edge : edges) {
            if (edge.getEndStation().equals(startStation)) {
                return edge;
            } else if (edge.getStartStation().equals(startStation)) {
                edge.reverse();
                return edge;
            }
        }
        return null;
    }
}
