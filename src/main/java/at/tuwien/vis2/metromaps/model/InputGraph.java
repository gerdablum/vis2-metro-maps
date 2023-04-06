package at.tuwien.vis2.metromaps.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.Iterator;
import java.util.List;

public class InputGraph {

    private Graph<Station, Edge> inputGraph;

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

    public void getBoundingBox() {
        // TODO calculate the bounding box of the input graph
        // I suggest to get the node most upper left and node most lower right and then calculate the bounding box around them
        // return height * width in meters
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
