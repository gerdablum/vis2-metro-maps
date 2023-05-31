package at.tuwien.vis2.metromaps.model.grid;

import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;
import at.tuwien.vis2.metromaps.model.Utils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class GridGraph {

    private final Graph<GridVertex, GridEdge> gridGraph;
    private double d = 0.4; // threshold (in km) we would like to have between each station
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

                    vertex12.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_180)));
                    vertex2.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_45)));
                    vertex3.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_90)));
                    vertex4.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_135)));
                    vertex6.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_180)));
                    vertex8.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_135)));
                    vertex9.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_90)));
                    vertex10.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_45)));
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
        logger.info("Routing a path from " + sourceFromInputGraph.getName() + " to " + targetFromInputGraph.getName());
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
                    //logger.info("grid vertex " + vertex.getName() + " with distance " + distanceSource + " gets penalty " + penalty);
                    edgesCandidates.forEach(edge -> edge.updateCosts(penalty));
                } else {
                    targetCandidates.add(vertex);
                    // set penalty for targets
                    Set<GridEdge> edgesCandidates = gridGraph.incomingEdgesOf(vertex);
                    double penalty = calculateDistancePenalty(targetFromInputGraph.getCoordinates(), vertex.getCoordinates());
                    //logger.info("grid vertex " + vertex.getName() + " with distance " + distanceTarget + " gets penalty " + penalty);
                    edgesCandidates.forEach(edge ->edge.updateCosts(penalty));
                }
            }
        });
        List<GridEdge> shortestPath = getShortestPathBetweenTwoSets(filterForAlreadyUsedVertices(sourceCandidates, sourceFromInputGraph.getName()), targetCandidates);

        // set already used edges to inf and mark grid edge as taken
        shortestPath.forEach(edge -> {
            updateBendCosts(gridGraph.outgoingEdgesOf(edge.getDestination()), edge);
            edge.getSource().setTaken();
            // TODO cost update has no effect. maybe something is wrong with source/target? Or just the calculation..
        });
        logger.info("routed a path from " + shortestPath.get(0).getSource().getName() + " to " +
                shortestPath.get(shortestPath.size()-1).getDestination().getName() + " with " + shortestPath.size() + " steps");
        shortestPath.get(0).getSource().setTakenWith(sourceFromInputGraph.getName());
        shortestPath.get(shortestPath.size()-1).getDestination().setTakenWith(targetFromInputGraph.getName());

        // TODO all bend edges (edges in between ingoing and outgoing path of a vertex - smaller angle) have cost inf
        return shortestPath;
    }

    public static void updateBendCosts(Set<GridEdge> outgoingEdges, GridEdge incomingEdge) {
        //logger.info("Center vertex: " + incomingEdge.getSource().getName() + " to " + incomingEdge.getDestination().getName());
        incomingEdge.setCostsInf();
        int centerVertexX = incomingEdge.getDestination().getIndexX();
        int centerVertexY = incomingEdge.getDestination().getIndexY();

        int incomingVertexX = incomingEdge.getSource().getIndexX();
        int incomingVertexY = incomingEdge.getSource().getIndexY();

        int incomingEdgeDirectionX = centerVertexX - incomingVertexX;
        int incomingEdgeDirectionY = centerVertexY - incomingVertexY;

        outgoingEdges.forEach(e -> {
            int neighbourCenterX = e.getSource().getIndexX();
            int neighbourCenterY = e.getSource().getIndexY();

            int neighbourOutgoingX = e.getDestination().getIndexX();
            int neighbourOutgoingY = e.getDestination().getIndexY();

            int neighbourDirectionX = neighbourCenterX - neighbourOutgoingX;
            int neighbourDirectionY = neighbourCenterY - neighbourOutgoingY;

            double angle1 = Math.atan2(incomingEdgeDirectionY, incomingEdgeDirectionX);
            double angle2 = Math.atan2(neighbourDirectionY, neighbourDirectionX);
            double angle = Math.abs(Math.toDegrees(angle2 - angle1) % 180);
            //logger.info("Edge from vertex " + e.getSource().getName() + " to " +  e.getDestination().getName());
            //logger.info("Setting edge bend cost from " + e.getBendCost() + "  to " + edgeCost);
            if (angle == 45) {
                e.updateCosts(GridEdge.BendCost.C_45);
            } else if (angle == 90) {
                e.updateCosts(GridEdge.BendCost.C_90);
            } else if (angle == 135) {
                e.updateCosts(GridEdge.BendCost.C_135);
            } else {
                e.updateCosts(GridEdge.BendCost.C_180);
            }
        });


    }


    private Set<GridVertex> filterForAlreadyUsedVertices(Set<GridVertex> sourceCandidates, String sourceStationName) {
        Set<GridVertex> taken = sourceCandidates.stream().filter(GridVertex::isTaken).collect(Collectors.toSet());
        if (taken.isEmpty()) {
            return sourceCandidates;
        }
        Set<GridVertex> takenWithSourceStation = taken.stream().filter(g -> sourceStationName.equals(g.getStationName())).collect(Collectors.toSet());
        if (takenWithSourceStation.isEmpty()) {
            return taken;
        }
        return takenWithSourceStation;

    }

    private List<GridEdge> getShortestPathBetweenTwoSets(Set<GridVertex> sourceCandidates, Set<GridVertex> targetCandidates) {
        int shortestDistance = Integer.MAX_VALUE;
        GridVertex finalSource = null;
        GridVertex finalTarget = null;
        GraphPath<GridVertex, GridEdge> shortestPath = null;
        for (GridVertex source: sourceCandidates) {
            for(GridVertex target: targetCandidates) {
                GraphPath<GridVertex, GridEdge> path = DijkstraShortestPath.findPathBetween(gridGraph, source, target);
                // TODO use a better distance (chebyshev?)
                int length = path.getLength();
                if (length < shortestDistance) {
                    shortestPath = path;
                    shortestDistance = length;
                    finalSource = source;
                    finalTarget = target;

                }
            }
        }
        if (shortestPath == null) {
            logger.warn("No shortest path found!");
            return new ArrayList<>();
        }
        // The path source destination direction can be reversed (because path is undirected I guess)
        List<GridEdge> pathList = shortestPath.getEdgeList();
        if (!finalSource.equals(pathList.get(0).getSource())) {
            Collections.reverse(pathList);
        }
        logger.info("path list first edge source:" +
                shortestPath.getEdgeList().get(0).getSource().getName() +
                " shortest path chosen source:" + finalSource.getName());
        logger.info("path list last edge destination:" +
                shortestPath.getEdgeList().get(shortestPath.getEdgeList().size() -1).getDestination().getName() +
                " shortest path chosen destination:" +
                finalTarget.getName());
        return pathList;
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
