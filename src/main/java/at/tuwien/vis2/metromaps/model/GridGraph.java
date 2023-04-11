package at.tuwien.vis2.metromaps.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.Iterator;
import java.util.Optional;

public class GridGraph {

    private Graph<GridVertex, DefaultEdge> gridGraph;
    private double d = 0.5; // threshold (in km) we would like to have between each station
    private int numberOfVerticesHorizontal;
    private int numberOfVerticesVertical;

    public GridGraph(double widthInputGraph, double heightInputGraph) {

        calcSizeOfGridGraph(widthInputGraph, heightInputGraph);

        this.gridGraph = GraphTypeBuilder.<GridVertex, DefaultEdge> undirected().allowingMultipleEdges(false)
                .allowingSelfLoops(false).edgeClass(DefaultEdge.class).weighted(true).buildGraph();

        // add vertices
        for (int y = 0; y < numberOfVerticesVertical; y++) {
            for (int x = 0; x <numberOfVerticesHorizontal; x++) {

                gridGraph.addVertex(new GridVertex(x+""+y, x, y));
            }

        }
        // add edges
        for (int y = 0; y < numberOfVerticesVertical; y++) {
            for (int x = 0; x <numberOfVerticesHorizontal; x++) {

                int finalY = y;
                int finalX = x;
                Optional<GridVertex> currentVertex = gridGraph.vertexSet().stream()
                        .filter(vertex -> vertex.getIndexY() == finalY && vertex.getIndexX() == finalX).findFirst();
                if(currentVertex.isPresent()) {
                    Optional<GridVertex> vertex12 = gridGraph.vertexSet().stream()
                            .filter(vertex -> vertex.getIndexY() == finalY-1 && vertex.getIndexX() == finalX).findFirst();

                    Optional<GridVertex> vertex2 = gridGraph.vertexSet().stream()
                            .filter(vertex -> vertex.getIndexY() == finalY-1 && vertex.getIndexX() == finalX+1).findFirst();

                    Optional<GridVertex> vertex3 = gridGraph.vertexSet().stream()
                            .filter(vertex -> vertex.getIndexY() == finalY && vertex.getIndexX() == finalX+1).findFirst();

                    Optional<GridVertex> vertex4 = gridGraph.vertexSet().stream()
                            .filter(vertex -> vertex.getIndexY() == finalY+1 && vertex.getIndexX() == finalX+1).findFirst();

                    Optional<GridVertex> vertex6 = gridGraph.vertexSet().stream()
                            .filter(vertex -> vertex.getIndexY() == finalY+1 && vertex.getIndexX() == finalX).findFirst();

                    Optional<GridVertex> vertex8 = gridGraph.vertexSet().stream()
                            .filter(vertex -> vertex.getIndexY() == finalY+1 && vertex.getIndexX() == finalX-1).findFirst();

                    Optional<GridVertex> vertex9 = gridGraph.vertexSet().stream()
                            .filter(vertex -> vertex.getIndexY() == finalY-1 && vertex.getIndexX() == finalX).findFirst();

                    Optional<GridVertex> vertex10 = gridGraph.vertexSet().stream()
                            .filter(vertex -> vertex.getIndexY() == finalY-1 && vertex.getIndexX() == finalX-1).findFirst();

                    vertex12.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new DefaultEdge()));
                    vertex2.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v,  new DefaultEdge()));
                    vertex3.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v,  new DefaultEdge()));
                    vertex4.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v,  new DefaultEdge()));
                    vertex6.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v,  new DefaultEdge()));
                    vertex8.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v,  new DefaultEdge()));
                    vertex9.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v,  new DefaultEdge()));
                    vertex10.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v,  new DefaultEdge()));
                }
            }

        }
    }

    private void calcSizeOfGridGraph(double widthInputGraph, double heightInputGraph) {
        numberOfVerticesHorizontal =  (int) (widthInputGraph / d);
        numberOfVerticesVertical = (int) (heightInputGraph / d);
    }

    public void printToCommandLine() {
        Iterator<GridVertex> iter = new DepthFirstIterator<>(gridGraph);
        while (iter.hasNext()) {
            GridVertex vertex = iter.next();
            System.out.println("Vertex " +vertex.getName() );
        }
    }

    public Graph<GridVertex, DefaultEdge> getGraph() {
        return gridGraph;
    }
}
