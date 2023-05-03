package at.tuwien.vis2.metromaps.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;
import java.util.stream.Collectors;

public class InputGraph {

    private Graph<Station, MetroLineEdge> inputGraph;
    private double width;
    private double height;
    private double[] leftUpperCoordinates;
    private double[] leftLowerCoordinates;
    private double[] rightUpperCoordinates;

    public InputGraph() {
        this.inputGraph = GraphTypeBuilder.<Station, MetroLineEdge> undirected().allowingMultipleEdges(false)
                .allowingSelfLoops(false).edgeClass(MetroLineEdge.class).weighted(true).buildGraph();
    }

    public void addVerticesFromEdges(List<MetroLineEdge> edges) {
        for (MetroLineEdge edge: edges) {
            inputGraph.addVertex(edge.getStartStation());
            inputGraph.addVertex(edge.getEndStation());
            inputGraph.addEdge(edge.getStartStation(), edge.getEndStation(), edge);
        }
    }

    public void calcBoundingBox() {
        Set<Station> stations = inputGraph.vertexSet();
        List<double[]> latLons = stations.stream().map(Station::getCoordinates).toList();
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

        double[] leftUpper = new double[]{smallestLat, smallestLon};
        double[] leftLower = new double[]{smallestLat, largestLon};
        double[] rightUpper = new double[]{largestLat, smallestLon};
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

    public int getLdegForStation(Station station) {
        Set<MetroLineEdge> edges = inputGraph.incomingEdgesOf(station);
        int ldeg = 0;
        for (MetroLineEdge e: edges) {
            ldeg += e.getLineNames().size();
        }
        return ldeg;
    }

    public List<MetroLineEdge> sortEdges() {
        boolean containsDanglingVertices = true;
        List<MetroLineEdge> sortedEdges = new ArrayList<>();
        Station stationWithHighestLdeg = getStationWithHighestLdeg(false);
        stationWithHighestLdeg.setProcessingState(Station.ProcessingState.DANGLING);
        while (containsDanglingVertices) {

            Station danglingStationWithHighestLdeg = getStationWithHighestLdeg(true);
            Set<MetroLineEdge> adjacentEdges = inputGraph.incomingEdgesOf(danglingStationWithHighestLdeg);
            Set<Station> adjacentStationsSet = new HashSet<>();
            for (MetroLineEdge e : adjacentEdges) {
                Station target = inputGraph.getEdgeTarget(e);
                Station source = inputGraph.getEdgeSource(e);
                adjacentStationsSet.add(source);
                adjacentStationsSet.add(target);
            }
            // take all unprocessed nodes and sort after ldeg
            List<Station> adjacentStations = adjacentStationsSet.stream()
                            .filter(s -> s.getProcessingState() == Station.ProcessingState.UNPROCESSED).sorted((o1, o2) -> {
                                int ldeg1 = getLdegForStation(o1);
                                int ldeg2 = getLdegForStation(o2);
                                return ldeg2 - ldeg1;
                            }).toList();

            // add the edge that connects highestStation with orderd adjacent unprocessed edge
            adjacentStations.forEach(adjacentStation -> {
                adjacentStation.setProcessingState(Station.ProcessingState.DANGLING);
                sortedEdges.add(inputGraph.getEdge(danglingStationWithHighestLdeg, adjacentStation));
            });
            danglingStationWithHighestLdeg.setProcessingState(Station.ProcessingState.PROCESSED);

            containsDanglingVertices = inputGraph.vertexSet().stream()
                    .map(Station::getProcessingState)
                    .toList()
                    .contains(Station.ProcessingState.DANGLING);
        }
        return sortedEdges;

        // TODO sort graph also if it is not connected
    }

    private Station getStationWithHighestLdeg(boolean onlyTakeDanglings) {
        Set<Station> stations = inputGraph.vertexSet();
        if (onlyTakeDanglings) {
            stations = stations.stream().filter(station -> station.getProcessingState() == Station.ProcessingState.DANGLING).collect(Collectors.toSet());
        }
        return stations.stream()
                .max((o1, o2) -> {
            int ldeg1 = getLdegForStation(o1);
            int ldeg2 = getLdegForStation(o2);
            return ldeg1 - ldeg2;
        }).get();
    }

    public void printToCommandLine() {
        Iterator<Station> iter = new DepthFirstIterator<>(inputGraph);
        while (iter.hasNext()) {
            Station vertex = iter.next();
            System.out.println("Vertex " +vertex.getName() + " has edges: "
                                    + inputGraph.edgesOf(vertex).stream().map(e -> e.getStartStation() + " to " + e.getEndStation()).toList());
        }
    }
}
