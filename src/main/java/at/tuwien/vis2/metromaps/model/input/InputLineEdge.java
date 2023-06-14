package at.tuwien.vis2.metromaps.model.input;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Represents an edge in the input graph from station to station
 */
public class InputLineEdge {

    private String id;
    private InputStation startStation;
    private InputStation endStation;

    private double[][] coordinates;
    private List<InputLine> lines;

    /**
     * Creates an edge from start station to end station. Contains all lines that are travelling on this route.
     * @param id must be unique
     * @param startStation one station between the edge
     * @param endStation other station between the edge
     * @param coordinates start and end coordinates of the edge, in some cases also keypoint coordinates form the geografical line routing
     * @param lineNames all lines that travel between those two stations
     */
    public InputLineEdge(String id, InputStation startStation, InputStation endStation, double[][] coordinates, List<InputLine> lineNames) {
        this.id = id;
        this.startStation = startStation;
        this.endStation = endStation;
        this.coordinates = coordinates;
        this.lines = new ArrayList<>(lineNames);
    }

    /**
     * Creates an input edge without stations (vertices)
     * @param id must be unique
     * @param coordinates start and end coordinates of the edge, in some cases also keypoint coordinates form the geografical line routing
     * @param lineNames  all lines that travel on this way
     */
    public InputLineEdge(String id, double[][] coordinates, List<InputLine> lineNames) {
        this.id = id;
        this.coordinates = coordinates;
        this.lines = new ArrayList<>(lineNames);
    }

    /**
     *
     * @return unique id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return adjacent input station
     */
    public InputStation getStartStation() {
        return startStation;
    }

    /**
     *
     * @param startStation adjacent station. Start and end must not be equal (no loops alowed)
     */
    public void setStartStation(InputStation startStation) {
        this.startStation = startStation;
    }

    /**
     *
     * @return adjacent station
     */
    public InputStation getEndStation() {
        return endStation;
    }

    /**
     *
     * @param endStation adjacent station. Start and end must not be equal (no loops alowed)
     */
    public void setEndStation(InputStation endStation) {
        this.endStation = endStation;
    }

    /**
     *
     * @return lines travelling along the edge
     */
    public List<InputLine> getLines() {
        return lines;
    }

    /**
     *
     * @return geografic representation of the line
     */
    public double[][] getCoordinates() {
        return coordinates;
    }

    @Override
    public int hashCode() {
        return lines.hashCode() + startStation.hashCode() + endStation.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InputLineEdge) {
            if (this.id.equals(((InputLineEdge) obj).getId())) {
                return true;
            }
            boolean hasSameLineEdges = new HashSet<>(this.getLines()).containsAll(((InputLineEdge) obj).getLines());
            // TODO also check for equality for start == end && end == start??
            return hasSameLineEdges && (this.startStation.equals(((InputLineEdge) obj).getStartStation()) && this.endStation.equals(((InputLineEdge) obj).getEndStation()));
        }
        return false;
    }

    /**
     * swaps start and end station
     */
    public void reverse() {
        InputStation temp = this.endStation;
        this.endStation = this.startStation;
        this.startStation = temp;
    }
}
