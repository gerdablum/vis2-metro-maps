package at.tuwien.vis2.metromaps.api;

import at.tuwien.vis2.metromaps.model.AllSubwayStations;
import at.tuwien.vis2.metromaps.model.SubwayStation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class OSMService {

//http://overpass-turbo.eu/

    Logger logger = LoggerFactory.getLogger(OSMService.class);

    @Value("classpath:exports/vienna.geojson")
    Resource jsonData;

    public List<SubwayStation> parseJsonData() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AllSubwayStations subwayStations = objectMapper.readValue(jsonData.getFile(), AllSubwayStations.class);
            return subwayStations.getFeatures();
        } catch (IOException e) {
            logger.error("Error reading vienna json file", e);
            return null;
        }
    }
}
