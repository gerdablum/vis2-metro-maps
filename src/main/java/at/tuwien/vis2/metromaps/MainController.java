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

/**
 * REST Controller for the backend application
 */
@RestController
public class MainController {

    Logger logger = LoggerFactory.getLogger(MainController.class);
    @Autowired
    private MetroDataProvider metroDataProvider;

    @Autowired
    private OctalinearGraphCalculator octalinearGraphCalculator;


    /**
     * Endpoint to get all stations with name and coordinates
     * @param city name of the selected city
     * @return list with all stations for given city
     */
    @GetMapping("/{city}/stations")
    public List<InputStation> getAllStations(@PathVariable String city) {
        sanityCheckCity(city);
        List<InputStation> subwayStations = metroDataProvider.getAllStations(city);

        return subwayStations;
    }

    /**
     * All geografic lines for given city
     * @param lineId (optional) if not null, only edges for a specific line are returned
     * @param city  name of the selected city
     * @return list with all lines of given city
     */
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

    /**
     * returns entire grid graph for given city. Caution: Can be empty, if graph has not been calculated yet.
     * @param city name of the selected city
     * @return grid graph
     */
    @GetMapping("/{city}/gridgraph")
    public GridGraph getGridGraph(@PathVariable String city) {
        sanityCheckCity(city);
        return octalinearGraphCalculator.getGridGraph(city);
    }

    /**
     * Calculates octilinear grid graph, or returns already calculated graph if city, grid size and r has not changed.
     * @param city  name of the selected city
     * @param gridSize distance in km between each grid vertex (width of a grid cell)
     * @param distanceR search radius for source/target candidates
     * @return list of grid edges for all paths on all lines.
     */
    @GetMapping("/{city}/octilinear")
    public List<List<GridEdge>> getOctilinearGraph(@PathVariable String city, @RequestParam(required = false) double gridSize, @RequestParam(required = false) double distanceR) {
        sanityCheckCity(city);
        sanityCheckGridParameter(gridSize);
        sanityCheckGridParameter(distanceR);

        return octalinearGraphCalculator.calculateOutputGraph(city, gridSize, distanceR);
    }

    private void sanityCheckCity(String city) {
        if (!Utils.allCities.contains(city)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("City name %s is unknown", city));
        }
    }
    private void sanityCheckGridParameter(Object gridParameter) {
        if (!(gridParameter instanceof Double)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Ungültiger Parameter: %s. Der Parameter muss vom Typ double sein.", gridParameter));
        }
        else if (((Double) gridParameter) <= 0.1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s is too small.", gridParameter));
        }

        else if (((Double) gridParameter) > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s is too large.", gridParameter));
        }
    }
    private void sanityCheckLine(String lineId, String city) {
        List<String> allLineNames = metroDataProvider.getAllLineNames(city);
        if (!allLineNames.contains(lineId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Line %s is unknown in city %s", lineId, city));
        }
    }
}
