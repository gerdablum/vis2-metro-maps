package at.tuwien.vis2.metromaps;


import at.tuwien.vis2.metromaps.model.*;
import at.tuwien.vis2.metromaps.model.grid.GridEdge;
import at.tuwien.vis2.metromaps.model.grid.GridGraph;
import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MainController {

    Logger logger = LoggerFactory.getLogger(MainController.class);
    @Autowired
    private MetroDataProvider metroDataProvider;

    @Autowired
    private OctalinearGraphCalculator octalinearGraphCalculator;


    @GetMapping("/vienna/stations")
    public List<InputStation> getAllStations() {
        List<InputStation> subwayStations = metroDataProvider.getAllStations();

        return subwayStations;
    }
    @GetMapping("/vienna/lines")
    public List<InputLineEdge> getLines(@RequestParam(required = false) String lineId) {
        if (lineId == null) {
            return metroDataProvider.getAllGeograficEdges();
        } else {
            List<InputLineEdge> allEdgesForLine = metroDataProvider.getEdgesWithoutStationInformation(lineId);
            List<InputLineEdge> orderedEdges = metroDataProvider.getOrderedEdgesForLine(lineId);

            return metroDataProvider.getEdgesWithoutStationInformation(lineId);
        }
    }

    @GetMapping("/vienna/gridgraph")
    public GridGraph getGridGraph() {
        return octalinearGraphCalculator.getGridGraph();
    }

    @GetMapping("/vienna/octilinear")
    public List<List<GridEdge>> getOctilinearGraph() {
        return octalinearGraphCalculator.calculateOutputGraph();

    }
}
