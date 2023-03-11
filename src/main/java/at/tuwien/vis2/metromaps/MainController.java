package at.tuwien.vis2.metromaps;


import at.tuwien.vis2.metromaps.api.OSMService;
import at.tuwien.vis2.metromaps.model.SubwayStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MainController {

    Logger logger = LoggerFactory.getLogger(MainController.class);
    @Autowired
    private OSMService osmService;

    @GetMapping("/vienna")
    public List<SubwayStation> serveWelcomePage() {
        List<SubwayStation> subwayStations = osmService.parseJsonData();
        for (SubwayStation station : subwayStations) {
            logger.info(station.getStationName());
        }

        return subwayStations;
    }
}
