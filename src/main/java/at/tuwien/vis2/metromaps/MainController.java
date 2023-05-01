package at.tuwien.vis2.metromaps;


import at.tuwien.vis2.metromaps.model.Edge;
import at.tuwien.vis2.metromaps.model.MetroDataProvider;
import at.tuwien.vis2.metromaps.model.Station;
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

    @GetMapping("/vienna/stations")
    public List<Station> getAllStations() {
        List<Station> subwayStations = metroDataProvider.getAllStations();

        return subwayStations;
    }
    @GetMapping("/vienna/lines")
    public List<Edge> getLines(@RequestParam(required = false) String lineId) {
        if (lineId == null) {
            return metroDataProvider.getAllGeograficEdges();
        } else {
            List<Edge> allEdgesForLine = metroDataProvider.getEdgesWithoutStationInformation(lineId);
            List<Edge> orderedEdges = metroDataProvider.getOrderedEdgesForLine(lineId);

            return metroDataProvider.getEdgesWithoutStationInformation(lineId);
        }
    }

    //@GetMapping("/gridgraph")
    //public GridGraph getGridGraph() {
      //  return new GridGraph(10, 10);
   // }
}
