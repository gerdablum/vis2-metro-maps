package at.tuwien.vis2.metromaps.model.input;

import at.tuwien.vis2.metromaps.model.Utils;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an input graph with stations and lines that should be mapped onto the grid graph
 */
public class InputGraph {

    private Graph<InputStation, InputLineEdge> inputGraph;
    private double width;
    private double height;
    private double[] leftUpperCoordinates;
    private double[] leftLowerCoordinates;
    private double[] rightUpperCoordinates;

    /**
     * Creates an empty graph.
     */
    public InputGraph() {
        this.inputGraph = GraphTypeBuilder.<InputStation, InputLineEdge> undirected().allowingMultipleEdges(false)
                .allowingSelfLoops(false).edgeClass(InputLineEdge.class).weighted(true).buildGraph();
    }

    /**
     * Inserts start station as vertex, end station as vertex and line as an edge into the input graph
     * @param edges edge from DataProvider. Must have start and end station.
     */
    public void addEdgeAndSourceDestVertices(List<InputLineEdge> edges) {
        for (InputLineEdge edge: edges) {
            inputGraph.addVertex(edge.getStartStation());
            inputGraph.addVertex(edge.getEndStation());
            inputGraph.addEdge(edge.getStartStation(), edge.getEndStation(), edge);
        }
    }

    /**
     * Calculates the size of the input graph from the most left station coordinates, most right, most upper and so on.
     * Calculates the bounding box coordinates around the whole size.
     * Also calculates width and height of bounding box.
     * Must be calculated before creating grid graph.
     */
    public void calcBoundingBox() {
        Set<InputStation> stations = inputGraph.vertexSet();
        List<double[]> latLons = stations.stream().map(InputStation::getCoordinates).toList();
        double smallestLon = Integer.MAX_VALUE;
        double smallestLat = Integer.MAX_VALUE;
        double largestLon = 0;
        double largestLat = 0;
        for (double[] latLon:  latLons) {
            var lat = latLon[0];
            var lon = latLon[1];

            if (lat < smallestLat) {
                smallestLat = lat;
            }
            if (lat > largestLat) {
                largestLat = lat;
            }

            if (lon < smallestLon) {
                smallestLon = lon;
            }

            if (lon > largestLon) {
                largestLon = lon;
            }
        }

        double[] leftUpper = new double[]{largestLat, smallestLon};
        double[] leftLower = new double[]{smallestLat, smallestLon};
        double[] rightUpper = new double[]{largestLat, largestLon};
        this.leftUpperCoordinates = leftUpper;
        this.leftLowerCoordinates = leftLower;
        this.rightUpperCoordinates = rightUpper;
        this.width = Utils.getDistanceInKmTo(leftUpper, rightUpper);
        this.height = Utils.getDistanceInKmTo(leftUpper, leftLower);
    }

    /**
     * Needed to calculate size of grid graph
     * @return width of bounding box around input graph
     */
    public double getWidth() {
        return width;
    }

    /**
     * Needed to calculate size of grid graph
     * @return height of bounding box around input graph
     */
    public double getHeight() {
        return height;
    }

    /**
     * Needed to calculate size of grid graph
     * @return left upper coordinates of bounding box around input graph
     */
    public double[] getLeftUpperCoordinates() {
        return leftUpperCoordinates;
    }

    /**
     * Needed to calculate size of grid graph
     * @return left lower coordinates of bounding box around input graph
     */
    public double[] getLeftLowerCoordinates() {
        return leftLowerCoordinates;
    }

    /**
     * Needed to calculate size of grid graph
     * @return right upper coordinates of bounding box around input graph
     */
    public double[] getRightUpperCoordinates() {
        return rightUpperCoordinates;
    }

    private int getLdegForStation(InputStation station) {
        Set<InputLineEdge> edges = inputGraph.incomingEdgesOf(station);
        int ldeg = 0;
        for (InputLineEdge e: edges) {
            ldeg += e.getLines().size();
        }
        return ldeg;
    }

    /**
     * Sorts the edge according to the highest ldeg (line degree). Lines in a higher density area with higher degree
     * are considered first.
     * @return list of ordered edges, where the highest line degree comes first.
     */
    public List<InputLineEdge> sortEdges() {
        List<InputLineEdge> allEdges = new ArrayList<>(inputGraph.edgeSet());
        List<InputLineEdge> allSortedEdges = new ArrayList<>();
        while (!allEdges.isEmpty()) {
            List<InputLineEdge> partSortedEdges = new ArrayList<>(sortPartEdges(allEdges));
            allSortedEdges.addAll(partSortedEdges);

            if (partSortedEdges.isEmpty()) {
                // add remaining edges!
                allSortedEdges.addAll(allEdges);
                allEdges.clear();
            }

            partSortedEdges.forEach(allEdges::remove);
        }

        return allSortedEdges;
    }

    private List<InputLineEdge> sortPartEdges(List<InputLineEdge> allEdges) {
        List<InputLineEdge> sortedEdges = new ArrayList<>();
        boolean containsDanglingVertices = true;
        InputStation stationWithHighestLdeg = getStationWithHighestLdeg(false);
        stationWithHighestLdeg.setProcessingState(InputStation.ProcessingState.DANGLING);
        while (containsDanglingVertices) {

            InputStation danglingStationWithHighestLdeg = getStationWithHighestLdeg(true);
            Set<InputLineEdge> adjacentEdges = inputGraph.incomingEdgesOf(danglingStationWithHighestLdeg);
            Set<InputStation> adjacentStationsSet = new HashSet<>();
            for (InputLineEdge e : adjacentEdges) {
                if (inputGraph.containsEdge(e)) {
                    InputStation target = inputGraph.getEdgeTarget(e);
                    InputStation source = inputGraph.getEdgeSource(e);
                    adjacentStationsSet.add(source);
                    adjacentStationsSet.add(target);
                }

            }
            // take all unprocessed nodes and sort after ldeg
            List<InputStation> adjacentStations = adjacentStationsSet.stream()
                            .filter(s -> s.getProcessingState() == InputStation.ProcessingState.UNPROCESSED).sorted((o1, o2) -> {
                                int ldeg1 = getLdegForStation(o1);
                                int ldeg2 = getLdegForStation(o2);
                                return ldeg2 - ldeg1;
                            }).toList();

            // add the edge that connects highestStation with orderd adjacent unprocessed edge
            adjacentStations.forEach(adjacentStation -> {
                adjacentStation.setProcessingState(InputStation.ProcessingState.DANGLING);
                sortedEdges.add(inputGraph.getEdge(danglingStationWithHighestLdeg, adjacentStation));
            });
            danglingStationWithHighestLdeg.setProcessingState(InputStation.ProcessingState.PROCESSED);

            containsDanglingVertices = inputGraph.vertexSet().stream()
                    .map(InputStation::getProcessingState)
                    .toList()
                    .contains(InputStation.ProcessingState.DANGLING);
        }
        resetStationProcesingState();
        return sortedEdges;
    }

    private void resetStationProcesingState() {
        inputGraph.vertexSet().forEach(v -> {
            v.setProcessingState(InputStation.ProcessingState.UNPROCESSED);
        });
    }

    private InputStation getStationWithHighestLdeg(boolean onlyTakeDanglings) {
        Set<InputStation> stations = inputGraph.vertexSet();
        if (onlyTakeDanglings) {
            stations = stations.stream().filter(station -> station.getProcessingState() == InputStation.ProcessingState.DANGLING).collect(Collectors.toSet());
        }
        return stations.stream()
                .max((o1, o2) -> {
            int ldeg1 = getLdegForStation(o1);
            int ldeg2 = getLdegForStation(o2);
            return ldeg1 - ldeg2;
        }).get();
    }

    public void printToCommandLine() {
        Iterator<InputStation> iter = new DepthFirstIterator<>(inputGraph);
        while (iter.hasNext()) {
            InputStation vertex = iter.next();
            System.out.println("Vertex " +vertex.getName() + " has edges: "
                                    + inputGraph.edgesOf(vertex).stream().map(e -> e.getStartStation() + " to " + e.getEndStation()).toList());
        }
    }
}
