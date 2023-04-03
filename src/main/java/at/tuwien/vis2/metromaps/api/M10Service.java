package at.tuwien.vis2.metromaps.api;

import at.tuwien.vis2.metromaps.model.Edge;
import at.tuwien.vis2.metromaps.model.MetroDataProvider;
import at.tuwien.vis2.metromaps.model.Station;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class M10Service implements MetroDataProvider {


    Logger logger = LoggerFactory.getLogger(M10Service.class);
    private Resource data;

    private ObjectMapper objectMapper;
    private Map<String, Station> allStations;
    private Map<String, Edge> allEdges;

    @Autowired
    public M10Service(@Value("classpath:exports/UBAHNOGD_UBAHNHALTOGD.json") Resource data) {
        allStations = new HashMap<>();
        allEdges = new HashMap<>();
        objectMapper = new ObjectMapper();
        this.data = data;
        parseData();
    }

    private void parseData() {
        //objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            M10Features subwayStations = objectMapper.readValue(data.getFile(), M10Features.class);
            for(M10Features.Feature feature : subwayStations.getFeatures()) {
                if("Point".equals(feature.getType())) {
                    Station station = new Station(feature.getStationName(), feature.getId(), feature.getCoordinates()[0],
                            Arrays.asList(String.valueOf(feature.getLineName())));
                    allStations.put(station.getName(), station);
                }
                else if("LineString".equals(feature.getType())) {
                    Edge edge = new Edge(feature.getId(), feature.getCoordinates(), String.valueOf(feature.getLineName()));
                    allEdges.put(edge.getId(), edge);
                }
                else {
                    logger.warn(String.format("Warning: %s with type %s cannot be assigned",
                            feature.getId(), feature.getType()));
                }
            }
        } catch (IOException e) {
            logger.error("Error reading vienna json file", e);
        }
    }
    @Override
    public List<Station> getAllStations() {
        return allStations.values().stream().toList();
    }

    @Override
    public List<Edge> getAllEdges() {
        return allEdges.values().stream().toList();
    }

    @Override
    public List<Station> getAllStationsForLine(String lineName) {
        return null;
    }

    @Override
    public List<Edge> getLine(String lineName) {
        return null;
    }

    @Override
    public Station getStationById(String id) {
        return null;
    }

    @Override
    public List<Edge> getAllEdgesForLine(String lineId) {
        return allEdges.values().stream()
                .filter(edge -> edge.getLineName().equals(lineId))
                .collect(Collectors.toList());
    }
}
