package at.tuwien.vis2.metromaps.model;

import at.tuwien.vis2.metromaps.api.M10Service;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GridGraph {

    private final Graph<GridVertex, DefaultEdge> gridGraph;
    private double d = 0.5; // threshold (in km) we would like to have between each station
    private double r = 0.7; // distance between source and target candidates to match input edges onto grid
    private double costM = 0.5;  // move penalty
    private double costH = 1; // hop cost of using a grid edge
    private int numberOfVerticesHorizontal;
    private int numberOfVerticesVertical;

    Logger logger = LoggerFactory.getLogger(GridGraph.class);

    public GridGraph(double widthInputGraph, double heightInputGraph, double[] leftUpper, double[] leftLower, double[] rightUpper) {

        calcSizeOfGridGraph(widthInputGraph, heightInputGraph);

        this.gridGraph = GraphTypeBuilder.<GridVertex, DefaultEdge> undirected().allowingMultipleEdges(false)
                .allowingSelfLoops(false).edgeClass(DefaultEdge.class).weighted(true).buildGraph();
        double stepSizeLon = (leftLower[1] - leftUpper[1]) / numberOfVerticesVertical;
        double stepSizeLat = (rightUpper[0] - leftUpper[0]) / numberOfVerticesHorizontal;
        // add vertices
        double[] coordinates = leftUpper;
        for (int x = 0; x <= numberOfVerticesHorizontal; x++) {
            for (int y = 0; y <= numberOfVerticesVertical; y++) {

                coordinates = new double[]{leftUpper[0] + stepSizeLat*x, leftUpper[1] + stepSizeLon*y};
                gridGraph.addVertex(new GridVertex(x+","+y, x, y, coordinates));
            }
        }
        // add edges
        for (int x = 0; x < numberOfVerticesHorizontal; x++) {
            for (int y = 0; y <numberOfVerticesVertical; y++) {

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

                    vertex12.ifPresent(v -> {
                        gridGraph.addEdge(currentVertex.get(), v, new DefaultEdge());
                        logger.debug("Adding vertex from " + currentVertex.get().getName() + " to " + v.getName());
                    });
                    vertex2.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new DefaultEdge()));
                    vertex3.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new DefaultEdge()));
                    vertex4.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new DefaultEdge()));
                    vertex6.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new DefaultEdge()));
                    vertex8.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new DefaultEdge()));
                    vertex9.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new DefaultEdge()));
                    vertex10.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new DefaultEdge()));
                }
            }

        }
    }

    private void calcSizeOfGridGraph(double widthInputGraph, double heightInputGraph) {
        numberOfVerticesHorizontal =  (int) (widthInputGraph / d);
        numberOfVerticesVertical = (int) (heightInputGraph / d);
    }

    public void processInputEdge(MetroLineEdge edgeFromInputGraph, Station sourceFromInputGraph, Station targetFromInputGraph) {

        double[] sourcePosition = sourceFromInputGraph.getCoordinates();
        double[] targetPosition = targetFromInputGraph.getCoordinates();

        Set<GridVertex> sourceCandidates = new HashSet<>();
        Set<GridVertex> targetCandidates = new HashSet<>();
        gridGraph.vertexSet().forEach( vertex -> {
            // sink and souce candidates according to set distance r
            double distanceSource = Utils.getDistanceInKmTo(sourcePosition, vertex.getCoordinates());
            double distanceTarget = Utils.getDistanceInKmTo(targetPosition, vertex.getCoordinates());

            // build voroni diagram to deal with overlapping sink and target candidates
            if (distanceSource < r || distanceTarget < r) {
                if (distanceSource < distanceTarget) {
                    sourceCandidates.add(vertex);
                    // set penalty for source
                    Set<DefaultEdge> edgesCandidates = gridGraph.incomingEdgesOf(vertex);
                    double penalty = calculateDistancePenalty(sourceFromInputGraph.getCoordinates(), vertex.getCoordinates());
                    edgesCandidates.forEach(edge -> gridGraph.setEdgeWeight(edge, penalty));
                } else {
                    targetCandidates.add(vertex);
                    // set penalty for targets
                    Set<DefaultEdge> edgesCandidates = gridGraph.incomingEdgesOf(vertex);
                    double penalty = calculateDistancePenalty(targetFromInputGraph.getCoordinates(), vertex.getCoordinates());
                    edgesCandidates.forEach(edge -> gridGraph.setEdgeWeight(edge, penalty));
                }
            }
        });
        List<DefaultEdge> shortestPath = getShortestPathBetweenTwoSets(sourceCandidates, targetCandidates);

        // TODO marry the original source and target from input graph with the grid graph
        // TODO continue with 4.3
    }

    private List<DefaultEdge> getShortestPathBetweenTwoSets(Set<GridVertex> sourceCandidates, Set<GridVertex> targetCandidates) {
        int shortestDistance = Integer.MAX_VALUE;
        GraphPath<GridVertex, DefaultEdge> shortestPath = null;
        for (GridVertex source: sourceCandidates) {
            DijkstraShortestPath<GridVertex, DefaultEdge> dijkstraAlg =
                    new DijkstraShortestPath<>(gridGraph);
            ShortestPathAlgorithm.SingleSourcePaths<GridVertex, DefaultEdge> iPaths = dijkstraAlg.getPaths(source);
            for(GridVertex target: targetCandidates) {
                GraphPath<GridVertex, DefaultEdge> path = iPaths.getPath(target);
                // TODO use a better distance (chebyshev?)
                int length = path.getLength();
                if (length < shortestDistance) {
                    shortestPath = path;
                    shortestDistance = length;
                }
            }
        }
        return shortestPath.getEdgeList();
    }

    private double calculateDistancePenalty(double[] originalGridNotePosition, double[] candidateGridNotePosition) {
        double normalizedDistance = Utils.getDistanceInKmTo(originalGridNotePosition, candidateGridNotePosition) / d;
        return normalizedDistance * (costH + costM);

    }

    public void printToCommandLine() {
        Iterator<GridVertex> iter = new DepthFirstIterator<>(gridGraph);
        while (iter.hasNext()) {
            GridVertex vertex = iter.next();
            System.out.println("Vertex " + vertex.getName() );
        }
    }

   public Set<GridVertex> getGridVertices() {
        return  gridGraph.vertexSet();
   }

   public Set<GridEdge> getEdges() {
        Set<GridEdge> gridEdges = new HashSet<>();
        for(DefaultEdge defaultEdge: gridGraph.edgeSet()) {
            GridVertex source = gridGraph.getEdgeSource(defaultEdge);
            GridVertex target = gridGraph.getEdgeTarget(defaultEdge);
            GridEdge edge = new GridEdge(source, target);
            gridEdges.add(edge);
        }
        return gridEdges;
   }
}
