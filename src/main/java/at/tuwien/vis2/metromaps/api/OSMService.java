package at.tuwien.vis2.metromaps.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class OSMService {

//http://overpass-turbo.eu/

    Logger logger = LoggerFactory.getLogger(OSMService.class);

    @Value("classpath:exports/vienna.geojson")
    Resource stationData;
    @Value("classpath:exports/vienna-railway.geojson")
    Resource stationAndRoutesData;

    private List<SubwayStation> parseSubwayStationData() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AllSubwayStations subwayStations = objectMapper.readValue(stationData.getFile(), AllSubwayStations.class);
            return subwayStations.getFeatures();
        } catch (IOException e) {
            logger.error("Error reading vienna json file", e);
            return null;
        }
    }
    private List<SubwayObject> parseAllSubwayNodesData() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SubwayObject[] subwayObjects = objectMapper.readValue(stationAndRoutesData.getFile(), SubwayObject[].class);
            return Arrays.stream(subwayObjects).toList();
        } catch (IOException e) {
            logger.error("Error reading vienna json file", e);
            return null;
        }
    }
}
