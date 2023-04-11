package at.tuwien.vis2.metromaps.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class InputGraph {

    private Graph<Station, Edge> inputGraph;
    private double width;
    private double height;

    public InputGraph() {
        this.inputGraph = GraphTypeBuilder.<Station, Edge> undirected().allowingMultipleEdges(false)
                .allowingSelfLoops(false).edgeClass(Edge.class).weighted(true).buildGraph();
    }

    public void addVertices(List<Edge> edges) {
        for (Edge edge: edges) {
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
        this.width = Utils.getDistanceInKmTo(leftUpper, rightUpper);
        this.height = Utils.getDistanceInKmTo(leftUpper, leftLower);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
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
