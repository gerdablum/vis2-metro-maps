package at.tuwien.vis2.metromaps.model.input;

import at.tuwien.vis2.metromaps.model.Utils;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;
import java.util.stream.Collectors;

public class InputGraph {

    private Graph<InputStation, InputLineEdge> inputGraph;
    private double width;
    private double height;
    private double[] leftUpperCoordinates;
    private double[] leftLowerCoordinates;
    private double[] rightUpperCoordinates;

    public InputGraph() {
        this.inputGraph = GraphTypeBuilder.<InputStation, InputLineEdge> undirected().allowingMultipleEdges(false)
                .allowingSelfLoops(false).edgeClass(InputLineEdge.class).weighted(true).buildGraph();
    }

    public void addEdgeAndSourceDestVertices(List<InputLineEdge> edges) {
        for (InputLineEdge edge: edges) {
            inputGraph.addVertex(edge.getStartStation());
            inputGraph.addVertex(edge.getEndStation());
            inputGraph.addEdge(edge.getStartStation(), edge.getEndStation(), edge);
        }
    }

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

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double[] getLeftUpperCoordinates() {
        return leftUpperCoordinates;
    }

    public double[] getLeftLowerCoordinates() {
        return leftLowerCoordinates;
    }

    public double[] getRightUpperCoordinates() {
        return rightUpperCoordinates;
    }

    public int getLdegForStation(InputStation station) {
        Set<InputLineEdge> edges = inputGraph.incomingEdgesOf(station);
        int ldeg = 0;
        for (InputLineEdge e: edges) {
            ldeg += e.getLineNames().size();
        }
        return ldeg;
    }

    public List<InputLineEdge> sortEdges() {
        boolean containsDanglingVertices = true;
        List<InputLineEdge> sortedEdges = new ArrayList<>();
        InputStation stationWithHighestLdeg = getStationWithHighestLdeg(false);
        stationWithHighestLdeg.setProcessingState(InputStation.ProcessingState.DANGLING);
        while (containsDanglingVertices) {

            InputStation danglingStationWithHighestLdeg = getStationWithHighestLdeg(true);
            Set<InputLineEdge> adjacentEdges = inputGraph.incomingEdgesOf(danglingStationWithHighestLdeg);
            Set<InputStation> adjacentStationsSet = new HashSet<>();
            for (InputLineEdge e : adjacentEdges) {
                InputStation target = inputGraph.getEdgeTarget(e);
                InputStation source = inputGraph.getEdgeSource(e);
                adjacentStationsSet.add(source);
                adjacentStationsSet.add(target);
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

        // TODO sort graph also if it is not connected
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
