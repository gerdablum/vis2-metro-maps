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


/**
 * This class reads the geojson data from https://octi.cs.uni-freiburg.de/ for the metro stations and lines.
 * We currently provide data for the cities Berlin, Freiburg, London, Stuttgart, Vienna.
 */
@Service
public class PaperMetroDataService implements MetroDataProvider {

    Logger logger = LoggerFactory.getLogger(PaperMetroDataService.class);

    private HashMap<String, ResourceWrapper> resources;
    private ObjectMapper objectMapper;

    /**
     * When creating a new instance, the data from all cities is loaded into resource wrappers
     * where the stations and lines can be accessed.
     */
    @Autowired
    public PaperMetroDataService() {
        Resource berlin = new ClassPathResource("exports/berlin.json");
        Resource freiburg = new ClassPathResource("exports/freiburg.json");
        Resource london = new ClassPathResource("exports/london.json");
        Resource stuttgart = new ClassPathResource("exports/stuttgart.json");
        Resource vienna = new ClassPathResource("exports/wien.json");

        this.resources = new HashMap<>();
        resources.put(Utils.berlin, new ResourceWrapper(berlin));
        resources.put(Utils.freiburg, new ResourceWrapper(freiburg));
        resources.put(Utils.london, new ResourceWrapper(london));
        resources.put(Utils.stuttgart, new ResourceWrapper(stuttgart));
        resources.put(Utils.vienna, new ResourceWrapper(vienna));

        this.objectMapper = new ObjectMapper();
        loadData();
    }

    private void loadData() {
        for (Map.Entry<String, ResourceWrapper> entry : resources.entrySet()) {
            ResourceWrapper wrapper = entry.getValue();
            try {
                PaperFeatures subwayStations = objectMapper.readValue(wrapper.resource.getInputStream(), PaperFeatures.class);
                List<PaperFeatures.Feature> allPoints = Arrays.stream(subwayStations.getFeatures()).filter( a -> "Point".equals(a.getType())).toList();
                List<PaperFeatures.Feature> allLines = Arrays.stream(subwayStations.getFeatures()).filter( a -> "LineString".equals(a.getType())).toList();
                wrapper.allPoints = allPoints;
                wrapper.allLines = allLines;

                wrapper.allInputStations = new HashMap<>();
                for (PaperFeatures.Feature point : wrapper.allPoints) {
                    String name = normalizeStationName(point.getProperties().getStationLabel());
                    wrapper.allInputStations.put(point.getProperties().getId(),new InputStation(name,
                            point.getProperties().getId(), point.getCoordinates()[0], null));
                }

                wrapper.allInputLineEdges = new HashMap<>();
                for (PaperFeatures.Feature line : wrapper.allLines) {
                    List<PaperFeatures.Line> lines = line.getProperties().getLines();
                    List<InputLine> inputLine = lines.stream().map(l -> new InputLine(l.getLabel(), l.getColor())).collect(Collectors.toList());
                    String from = line.getProperties().getFrom();
                    String to = line.getProperties().getTo();
                    wrapper.allInputStations.get(from).addLines(inputLine);
                    wrapper.allInputStations.get(to).addLines(inputLine);
                    InputStation startStation = wrapper.allInputStations.get(from);
                    InputStation endStations = wrapper.allInputStations.get(to);
                    String id = line.getProperties().getId();
                    if (id == null) {
                        id = UUID.randomUUID().toString();
                    }
                    InputLineEdge lineEdge = new InputLineEdge(id, startStation, endStations,
                            line.getCoordinates(), inputLine);
                    wrapper.allInputLineEdges.put(id, lineEdge);
                }

            } catch (IOException e) {
                logger.error(String.format("importing of %s threw an exception:", wrapper.resource.getFilename()));
                throw new RuntimeException(e);
            }
        }

    }

    private String normalizeStationName(String stationLabel) {
        if (stationLabel == null) return null;
        String truncated = stationLabel.replaceAll("\\(.*?\\)", "");
        truncated = truncated.replaceAll("^U +|^S *\\+ *U +", "");
        return truncated;
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
        Map<String, InputStation> merged = new HashMap<>(resources.get(city).mergedStations);
        removeWeirdStations(orderedEdges, merged);
        resources.get(city).mergedStations = merged;
        orderedEdges = removeDuplicates(orderedEdges);
        return orderedEdges;
    }

    private LinkedList<InputLineEdge> removeDuplicates(LinkedList<InputLineEdge> orderedEdges) {
        LinkedList<InputLineEdge> edges = new LinkedList<>(orderedEdges);
        for (InputLineEdge edge: orderedEdges) {
            if (edge.getStartStation().equals(edge.getEndStation())) {
                edges.remove(edge);
            }
        }
        return edges;
    }

    private void removeWeirdStations(LinkedList<InputLineEdge> orderedEdges, Map<String, InputStation> mergedStations) {
        updateEdgeList(orderedEdges, mergedStations);
        if (isWeirdNode(orderedEdges.getFirst().getStartStation())) {
            String weirdNodeName = orderedEdges.getFirst().getStartStation().getName();
            InputStation mergedInto = orderedEdges.getFirst().getEndStation();
            mergedStations.put(weirdNodeName, mergedInto);
            orderedEdges.removeFirst();
            updateEdgeList(orderedEdges, mergedStations);
        }

        if(isWeirdNode(orderedEdges.getLast().getEndStation())) {
            String weirdNodeName = orderedEdges.getLast().getEndStation().getName();
            InputStation mergedInto = orderedEdges.getLast().getStartStation();
            mergedStations.put(weirdNodeName, mergedInto);
            orderedEdges.removeLast();
            updateEdgeList(orderedEdges, mergedStations);
        }
        for (int i = 0; i < orderedEdges.size() - 1; i++) {
            InputLineEdge edge = orderedEdges.get(i);
            InputLineEdge nextEdge = orderedEdges.get(i+1);

            if (isWeirdNode(edge.getEndStation()) && edge.getEndStation().equals(nextEdge.getStartStation())) {
                String weirdNodeName = edge.getEndStation().getName();
                InputStation mergedInto = nextEdge.getEndStation();
                mergedStations.put(weirdNodeName, mergedInto);
                edge.setEndStation(nextEdge.getEndStation());
                orderedEdges.remove(nextEdge);
                updateEdgeList(orderedEdges, mergedStations);
            }
        }
    }

    private static void updateEdgeList(LinkedList<InputLineEdge> orderedEdges, Map<String, InputStation> mergedStations) {
        orderedEdges.forEach(edge -> {
            InputStation start = edge.getStartStation();
            InputStation end = edge.getEndStation();
            InputStation mergedStart = mergedStations.get(start.getName());
            InputStation mergedEnd = mergedStations.get(end.getName());
            if (mergedStart != null) {
                edge.setStartStation(mergedStart);
            }
            if (mergedEnd != null) {
                edge.setEndStation(mergedEnd);
            }
        });
    }

    private boolean isWeirdNode(InputStation station) {
        return station.getName().equals(station.getId());
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
