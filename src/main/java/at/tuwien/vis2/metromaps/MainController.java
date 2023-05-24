package at.tuwien.vis2.metromaps;


import at.tuwien.vis2.metromaps.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class MainController {

    Logger logger = LoggerFactory.getLogger(MainController.class);
    @Autowired
    private MetroDataProvider metroDataProvider;

    @Autowired
    private OctalinearGraphCalculator octalinearGraphCalculator;


    @GetMapping("/vienna/stations")
    public List<Station> getAllStations() {
        List<Station> subwayStations = metroDataProvider.getAllStations();

        return subwayStations;
    }
    @GetMapping("/vienna/lines")
    public List<MetroLineEdge> getLines(@RequestParam(required = false) String lineId) {
        if (lineId == null) {
            return metroDataProvider.getAllGeograficEdges();
        } else {
            List<MetroLineEdge> allEdgesForLine = metroDataProvider.getEdgesWithoutStationInformation(lineId);
            List<MetroLineEdge> orderedEdges = metroDataProvider.getOrderedEdgesForLine(lineId);

            return metroDataProvider.getEdgesWithoutStationInformation(lineId);
        }
    }

    @GetMapping("/vienna/gridgraph")
    public GridGraph getGridGraph() {
        InputGraph inputGraph = new InputGraph();
        // TODO get this linenames from metroDataProvider
        List<String> lineNamesInVienna = Arrays.asList("1", "2", "3", "4", "6");
        for (String lineName: lineNamesInVienna) {
            List<MetroLineEdge> orderedEdgesForLine = metroDataProvider.getOrderedEdgesForLine(lineName);
            inputGraph.addEdgeAndSourceDestVertices(orderedEdgesForLine);
        }
        inputGraph.calcBoundingBox();
        return new GridGraph(inputGraph.getWidth(), inputGraph.getHeight(), inputGraph.getLeftUpperCoordinates(),
                inputGraph.getLeftLowerCoordinates(), inputGraph.getRightUpperCoordinates());
    }

    @GetMapping("/vienna/octilinear")
    public List<List<GridEdge>> getOctilinearGraph() {
        return octalinearGraphCalculator.calculateOutputGraph();

    }
}
