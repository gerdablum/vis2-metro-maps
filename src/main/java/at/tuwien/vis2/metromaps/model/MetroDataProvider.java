package at.tuwien.vis2.metromaps.model;

import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;

import java.util.List;

/**
 * Provides stations of type InputStation and edges of type InputEdge from any input source
 */
public interface MetroDataProvider {

    /**
     * Returns all stations for a given city
     * @param city name (key) of the city which is selected
     * @return List of all input stations.
     */
    List<InputStation> getAllStations(String city);

    /**
     * returns all input edges, not necessarily with a linking to the stations but with geografic line information.
     * @param city name (key) of the city which is selected
     * @return all input edges for the given gity
     */
    List<InputLineEdge> getAllGeograficEdges(String city);

    /**
     * same as `getAllGeograficEdges` but only returns edges for a specific line
     * @param lineId line name (e.g. U4)
     * @param city name (key) of the city which is selected
     * @return list of input line edges with coordinates but not necessarily with start and end station
     */
    List<InputLineEdge> getAllGeograficEdgesForLine(String lineId, String city);

    /**
     * Orders all input edges according to the original line ordering and establishes linking between stations and edges
     * as well as between edges.
     * @param lineId line name that should be ordered
     * @param city name (key) of the city which is selected
     * @return List of ordered and linked input edges where start and end edges match, e.g. [A -> B, B -> C, C -> D...]
     */
    List<InputLineEdge> getOrderedEdgesForLine(String lineId, String city);

    /**
     *
     * @param city  name (key) of the city which is selected
     * @return all line names for a given city
     */
    List<String> getAllLineNames(String city);
}
