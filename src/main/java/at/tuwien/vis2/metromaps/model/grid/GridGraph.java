package at.tuwien.vis2.metromaps.model.grid;

import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;
import at.tuwien.vis2.metromaps.model.Utils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class GridGraph {

    private final Graph<GridVertex, GridEdge> gridGraph;
    private double d = 0.5; // threshold (in km) we would like to have between each station
    private double r = 0.8; // distance between source and target candidates to match input edges onto grid
    private double costM = 0.5;  // move penalty
    private double costH = 1; // hop cost of using a grid edge
    private int numberOfVerticesHorizontal;
    private int numberOfVerticesVertical;

    Logger logger = LoggerFactory.getLogger(GridGraph.class);

    public GridGraph(double widthInputGraph, double heightInputGraph, double[] leftUpper, double[] leftLower, double[] rightUpper) {

        calcSizeOfGridGraph(widthInputGraph, heightInputGraph);

        this.gridGraph = GraphTypeBuilder.<GridVertex, GridEdge> undirected().allowingMultipleEdges(false)
                .allowingSelfLoops(false).edgeClass(GridEdge.class).weighted(true).buildGraph();
        double stepSizeLon = (rightUpper[1] - leftUpper[1]) / numberOfVerticesVertical;
        double stepSizeLat = (leftUpper[0] - leftLower[0]) / numberOfVerticesHorizontal;
        // add vertices
        double[] coordinates = leftUpper;
        for (int y = 0; y <= numberOfVerticesVertical; y++) {
            for (int x = 0; x <= numberOfVerticesHorizontal; x++) {

                coordinates = new double[]{leftUpper[0] - stepSizeLat*x, leftUpper[1] + stepSizeLon*y};
                gridGraph.addVertex(new GridVertex(x+","+y, x, y, coordinates));
                if (coordinates[0] == 48.27751786569251 && coordinates[1] == 16.452139552735247) {
                    logger.info("Alarm! Weird vertex inserted!");
                }
            }
        }
        // add edges
        for (int y = 0; y <= numberOfVerticesVertical; y++) {
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

                    vertex12.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, 0)));
                    vertex2.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, 45)));
                    vertex3.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, 90)));
                    vertex4.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, 135)));
                    vertex6.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, 180)));
                    vertex8.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, 225)));
                    vertex9.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, 270)));
                    vertex10.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, 315)));
                }
            }

        }
    }

    private void calcSizeOfGridGraph(double widthInputGraph, double heightInputGraph) {
        numberOfVerticesHorizontal =  (int) Math.ceil(widthInputGraph / d);
        numberOfVerticesVertical = (int) Math.ceil(heightInputGraph / d);
    }

    public List<GridEdge> processInputEdge(InputLineEdge edgeFromInputGraph, InputStation sourceFromInputGraph, InputStation targetFromInputGraph) {

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
                    Set<GridEdge> edgesCandidates = gridGraph.incomingEdgesOf(vertex);
                    double penalty = calculateDistancePenalty(sourceFromInputGraph.getCoordinates(), vertex.getCoordinates());
                    edgesCandidates.forEach(edge -> gridGraph.setEdgeWeight(edge, penalty));
                } else {
                    targetCandidates.add(vertex);
                    // set penalty for targets
                    Set<GridEdge> edgesCandidates = gridGraph.incomingEdgesOf(vertex);
                    double penalty = calculateDistancePenalty(targetFromInputGraph.getCoordinates(), vertex.getCoordinates());
                    edgesCandidates.forEach(edge -> gridGraph.setEdgeWeight(edge, penalty));
                }
            }
        });
        List<GridEdge> shortestPath = getShortestPathBetweenTwoSets(filterForAlreadyUsedVertices(sourceCandidates), targetCandidates);

        // set already used edges to inf and mark grid edge as taken
        shortestPath.forEach(edge -> {
            gridGraph.setEdgeWeight(edge, Double.MAX_VALUE);
            edge.getSource().setTakenWith(sourceFromInputGraph.getName());
            edge.getDestination().setTakenWith(targetFromInputGraph.getName());
            updateBendCosts(gridGraph.outgoingEdgesOf(edge.getDestination()), edge);
            // TODO cost update has no effect. maybe something is wrong with source/target?
        });

        // TODO all bend edges (edges in between ingoing and outgoing path of a vertex - smaller angle) have cost inf
        return shortestPath;
    }

    private void updateBendCosts(Set<GridEdge> outgoingEdges, GridEdge incomingEdge) {
        int bendCostOfUsedEdge = incomingEdge.getBendCost();
        // the larger the angle the lower the costs
        outgoingEdges.forEach(e -> {
            double edgeCost = 180 - ((e.getBendCost() + incomingEdge.getBendCost()) % 180);
           // logger.info("Setting edge bend cost from " + e.getBendCost() + "  to " + edgeCost);
            gridGraph.setEdgeWeight(e, edgeCost);
        });


    }


    private Set<GridVertex> filterForAlreadyUsedVertices(Set<GridVertex> sourceCandidates) {
        Set<GridVertex> taken = sourceCandidates.stream().filter(GridVertex::isTaken).collect(Collectors.toSet());
        if (taken.isEmpty()) {
            return sourceCandidates;
        }
        return taken;

    }

    private List<GridEdge> getShortestPathBetweenTwoSets(Set<GridVertex> sourceCandidates, Set<GridVertex> targetCandidates) {
        int shortestDistance = Integer.MAX_VALUE;
        GraphPath<GridVertex, GridEdge> shortestPath = null;
        for (GridVertex source: sourceCandidates) {
            DijkstraShortestPath<GridVertex, GridEdge> dijkstraAlg =
                    new DijkstraShortestPath<>(gridGraph);
            ShortestPathAlgorithm.SingleSourcePaths<GridVertex, GridEdge> iPaths = dijkstraAlg.getPaths(source);
            for(GridVertex target: targetCandidates) {
                GraphPath<GridVertex, GridEdge> path = iPaths.getPath(target);
                // TODO use a better distance (chebyshev?)
                int length = path.getLength();
                if (length < shortestDistance) {
                    shortestPath = path;
                    shortestDistance = length;

                }
            }
        }
        if (shortestPath == null) {
            logger.warn("No shortest path found!");
            return new ArrayList<>();
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
        return gridGraph.edgeSet();
   }
}
