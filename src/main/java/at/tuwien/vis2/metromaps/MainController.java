package at.tuwien.vis2.metromaps;


import at.tuwien.vis2.metromaps.model.*;
import at.tuwien.vis2.metromaps.model.grid.GridEdge;
import at.tuwien.vis2.metromaps.model.grid.GridGraph;
import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class MainController {

    Logger logger = LoggerFactory.getLogger(MainController.class);
    @Autowired
    private MetroDataProvider metroDataProvider;

    @Autowired
    private OctalinearGraphCalculator octalinearGraphCalculator;


    @GetMapping("/{city}/stations")
    public List<InputStation> getAllStations(@PathVariable String city) {
        sanityCheckCity(city);
        List<InputStation> subwayStations = metroDataProvider.getAllStations(city);

        return subwayStations;
    }
    @GetMapping("/{city}/lines")
    public List<InputLineEdge> getLines(@RequestParam(required = false) String lineId, @PathVariable String city) {
        sanityCheckCity(city);
        if (lineId == null) {
            return metroDataProvider.getAllGeograficEdges(city);
        } else {
            sanityCheckLine(lineId, city);

            return metroDataProvider.getAllGeograficEdgesForLine(lineId, city);
        }
    }



    @GetMapping("/{city}/gridgraph")
    public GridGraph getGridGraph(@PathVariable String city) {
        sanityCheckCity(city);
        return octalinearGraphCalculator.getGridGraph(city);
    }

    @GetMapping("/{city}/octilinear")
    public List<List<GridEdge>> getOctilinearGraph(@PathVariable String city) {
        sanityCheckCity(city);
        return octalinearGraphCalculator.calculateOutputGraph(city);

    }

    private void sanityCheckCity(String city) {
        if (!Utils.allCities.contains(city)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("City name %s is unknown", city));
        }

    }

    private void sanityCheckLine(String lineId, String city) {
        List<String> allLineNames = metroDataProvider.getAllLineNames(city);
        if (!allLineNames.contains(lineId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Line %s is unknown in city %s", lineId, city));
        }
    }
}
