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
    private double d = 0.3; // threshold (in km) we would like to have between each grid cell
    private double r = 1.5; // distance between source and target candidates to match input edges onto grid
    private double costM = 0.5;  // move penalty
    private double costH = 1; // hop cost of using a grid edge
    private int numberOfVerticesHorizontal;
    private int numberOfVerticesVertical;

    Logger logger = LoggerFactory.getLogger(GridGraph.class);

    public GridGraph(double widthInputGraph, double heightInputGraph, double[] leftUpper, double[] leftLower, double[] rightUpper) {

        calcSizeOfGridGraph(widthInputGraph, heightInputGraph);

        this.gridGraph = GraphTypeBuilder.<GridVertex, GridEdge> undirected().allowingMultipleEdges(false)
                .allowingSelfLoops(false).edgeClass(GridEdge.class).weighted(true).buildGraph();
        double stepSizeLat = (leftUpper[0] - leftLower[0]) / numberOfVerticesVertical;          // distance between lat lines
        double stepSizeLon = (rightUpper[1] - leftUpper[1]) / numberOfVerticesHorizontal;       // distance between lon lines
        // add vertices
        double[] coordinates = leftUpper;
        for (int y = 0; y <= numberOfVerticesVertical; y++) {
            for (int x = 0; x <= numberOfVerticesHorizontal; x++) {

                coordinates = new double[]{leftUpper[0] - stepSizeLat*y, leftUpper[1] + stepSizeLon*x};
                gridGraph.addVertex(new GridVertex(x+","+y, x, y, coordinates));

            }
        }
        // (Latitude, Longitude); lat=horizontal,lon=vertikal
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
                    vertex2.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_180)));
                    vertex3.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_180)));
                    vertex4.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_180)));
                    vertex6.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_180)));
                    vertex8.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_180)));
                    vertex9.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_180)));
                    vertex10.ifPresent(v -> gridGraph.addEdge(currentVertex.get(), v, new GridEdge(currentVertex.get(), v, GridEdge.BendCost.C_180)));
                }
            }

        }
    }

    private void calcSizeOfGridGraph(double widthInputGraph, double heightInputGraph) {
        numberOfVerticesHorizontal =  (int) Math.ceil(widthInputGraph / d);
        numberOfVerticesVertical = (int) Math.ceil(heightInputGraph / d);
    }

    public GraphPath<GridVertex, GridEdge> processInputEdge(InputLineEdge edgeFromInputGraph, InputStation sourceFromInputGraph, InputStation targetFromInputGraph, String lineName) {
        if (sourceFromInputGraph.getName().equals("0x13434420") || targetFromInputGraph.getName().equals("Großfeldsiedlung")) {
            int a = 1;
        }

        logger.info("Routing a path from " + sourceFromInputGraph.getName() + " to " + targetFromInputGraph.getName());
        Set<GridVertex> sourceCandidates = new HashSet<>();
        Set<GridVertex> targetCandidates = new HashSet<>();
        boolean sourceIsFixed = false;
        boolean targetIsFixed = false;
        Set<GridVertex> allGridVertices = new HashSet<>(gridGraph.vertexSet());
        GridVertex takenSource = findAlreadyRoutedGridVertex(allGridVertices, sourceFromInputGraph.getName());
        GridVertex takenTarget = findAlreadyRoutedGridVertex(allGridVertices, targetFromInputGraph.getName());
        if (takenSource != null) {
            sourceCandidates.add(takenSource);
            UpdateGridGraphCost(gridGraph.outgoingEdgesOf(takenSource), 1);
            allGridVertices.remove(takenSource);
            sourceIsFixed = true;
        }
        if (takenTarget != null) {
            targetCandidates.add(takenTarget);
            allGridVertices.remove(takenTarget);
            UpdateGridGraphCost(gridGraph.outgoingEdgesOf(takenTarget), 1);
            targetIsFixed = true;
        }


        searchCandidatesAndCalculateOffsetcosts(allGridVertices, sourceFromInputGraph, targetFromInputGraph, sourceCandidates, targetCandidates);
        if (sourceIsFixed) {
            // our source is already routed in the path. So we reset the offset costs from all other source candidates
            // (offset cost must not influence routing in this case!)
            sourceCandidates.forEach(v -> UpdateGridGraphCost(gridGraph.outgoingEdgesOf(v), 1));
        } else if (targetIsFixed) {
            targetCandidates.forEach(v -> UpdateGridGraphCost(gridGraph.outgoingEdgesOf(v), 1));
        }


        GraphPath<GridVertex, GridEdge> shortestPath = getShortestPathBetweenTwoSets(filterForAlreadyUsedVertices(sourceCandidates, sourceFromInputGraph.getName()),
                filterForAlreadyUsedVertices(targetCandidates, targetFromInputGraph.getName()));
       if (shortestPath == null) {
           return null;
       }
        // set already used edges to inf and mark grid edge as taken
        shortestPath.getEdgeList().forEach(gridEdge -> {
            gridEdge.setTaken(edgeFromInputGraph.getLineNames(), lineName);
            gridEdge.setCostsInf();
            gridGraph.setEdgeWeight(gridEdge, gridEdge.getCosts());
        });
        // update available & taken lines for the vertices
        List<GridVertex> vertexList = shortestPath.getVertexList();
        vertexList.remove(shortestPath.getEndVertex());
        for (GridVertex vertex : vertexList) {
            vertex.setTakenLineNames(sourceFromInputGraph.getLineNames(), lineName);
            gridGraph.outgoingEdgesOf(vertex).forEach(v -> v.setCostsInf());
        }
        shortestPath.getEndVertex().setTakenLineNames(targetFromInputGraph.getLineNames(), lineName);


        GridEdge lastEdgeInPath = shortestPath.getEdgeList().get(shortestPath.getEdgeList().size() -1);
        updateBendCosts(gridGraph.outgoingEdgesOf(shortestPath.getEndVertex()), lastEdgeInPath);

        logger.info("routed a path from " + shortestPath.getStartVertex().getName() + " to " +
                shortestPath.getEndVertex().getName() + " with " + shortestPath.getEdgeList().size() + " steps");
        shortestPath.getStartVertex().setTakenWith(sourceFromInputGraph.getName());
        shortestPath.getEndVertex().setTakenWith(targetFromInputGraph.getName());

        // TODO all bend edges (edges in between ingoing and outgoing path of a vertex - smaller angle) have cost inf

        return shortestPath;
    }

    private GridVertex findAlreadyRoutedGridVertex(Set<GridVertex> gridVertices, String name) {
        Optional<GridVertex> first = gridVertices.stream()
                .filter(v -> v.isTaken() && v.getStationName().equals(name)).findFirst();
        return first.orElse(null);
    }

    private void searchCandidatesAndCalculateOffsetcosts(Set<GridVertex> allGridVertices, InputStation sourceFromInputGraph, InputStation targetFromInputGraph, Set<GridVertex> sourceCandidates, Set<GridVertex> targetCandidates) {
        double[] sourcePosition = sourceFromInputGraph.getCoordinates();
        double[] targetPosition = targetFromInputGraph.getCoordinates();
        for (GridVertex vertex : allGridVertices) {// sink and source candidates according to set distance r
            double distanceSource = Utils.getDistanceInKmTo(sourcePosition, vertex.getCoordinates());
            double distanceTarget = Utils.getDistanceInKmTo(targetPosition, vertex.getCoordinates());
                // build voroni diagram to deal with overlapping sink and target candidates
            if (vertex.getName().equals("35,4")) {
                int a = 1;
            }
            if (distanceSource < r || distanceTarget < r) {
                if (distanceSource < distanceTarget) {
                    sourceCandidates.add(vertex);
                    // set penalty for source
                    Set<GridEdge> edgesCandidates = gridGraph.incomingEdgesOf(vertex);
                    double offsetcosts = calculateDistancePenalty(sourceFromInputGraph.getCoordinates(), vertex.getCoordinates());
                    //logger.info("grid vertex " + vertex.getName() + " with distance " + distanceSource + " gets penalty " + penalty);
                    UpdateGridGraphCost(edgesCandidates, offsetcosts);

                } else {
                    targetCandidates.add(vertex);
                    // set penalty for targets
                    Set<GridEdge> edgesCandidates = gridGraph.incomingEdgesOf(vertex);
                    double offsetcosts = calculateDistancePenalty(targetFromInputGraph.getCoordinates(), vertex.getCoordinates());
                    //logger.info("grid vertex " + vertex.getName() + " with distance " + distanceTarget + " gets penalty " + penalty);
                    UpdateGridGraphCost(edgesCandidates, offsetcosts);
                }
            }
        }
    }

    private void UpdateGridGraphCost(Set<GridEdge> edgesCandidates, double penalty) {
        edgesCandidates.forEach(edge -> {
            edge.updateCosts(penalty);
            gridGraph.setEdgeWeight(edge, edge.getCosts());
        });
    }

    public void updateBendCosts(Set<GridEdge> outgoingEdges, GridEdge incomingEdge) {
        incomingEdge.setCostsInf();
        int centerVertexX = incomingEdge.getDestination().getIndexX();
        int centerVertexY = incomingEdge.getDestination().getIndexY();

        int incomingVertexX = incomingEdge.getSource().getIndexX();
        int incomingVertexY = incomingEdge.getSource().getIndexY();

        int incomingEdgeDirectionX = incomingVertexX - centerVertexX;
        int incomingEdgeDirectionY = incomingVertexY - centerVertexY;

        outgoingEdges.forEach(e -> {
            int neighbourCenterX = e.getSource().getIndexX();
            int neighbourCenterY = e.getSource().getIndexY();

            int neighbourOutgoingX = e.getDestination().getIndexX();
            int neighbourOutgoingY = e.getDestination().getIndexY();

            // directions must match!!!
            if (centerVertexX == neighbourOutgoingX && centerVertexY == neighbourOutgoingY) {
                int tempx = neighbourCenterX;
                int tempy = neighbourCenterY;
                neighbourCenterX = neighbourOutgoingX;
                neighbourCenterY = neighbourOutgoingY;
                neighbourOutgoingX = tempx;
                neighbourOutgoingY = tempy;
            }

            int neighbourDirectionX = neighbourOutgoingX - neighbourCenterX;
            int neighbourDirectionY = neighbourOutgoingY - neighbourCenterY;

            if(neighbourCenterX == 21 && neighbourCenterY == 20) {
                int a = 1;
                if(neighbourCenterX == 21 && neighbourCenterY == 20) {
                }
            }

            double angle1 = Math.atan2(-incomingEdgeDirectionY, incomingEdgeDirectionX);
            double angle2 = Math.atan2(-neighbourDirectionY, neighbourDirectionX);
            double angle = Math.toDegrees(angle2 - angle1);
            //logger.info("Edge from vertex " + e.getSource().getName() + " to " +  e.getDestination().getName());
            //logger.info("Setting edge bend cost from " + e.getBendCost() + "  to " + edgeCost);
            if (angle < 0) {
                angle += 360;
            } else if (angle > 360) {
                angle -= 360;
            }
            if (angle > 180) {
                angle = 360 - angle;
            }

            if (angle == 45) {
                e.updateCosts(GridEdge.BendCost.C_45);
            } else if (angle == 90) {
                e.updateCosts(GridEdge.BendCost.C_90);
            } else if (angle == 135) {
                e.updateCosts(GridEdge.BendCost.C_135);
            } else {
                e.updateCosts(GridEdge.BendCost.C_180);
            }
            gridGraph.setEdgeWeight(e, e.getCosts());
        });
    }

    private Set<GridVertex> filterForAlreadyUsedVertices(Set<GridVertex> sourceCandidates, String sourceStationName) {
        // this is somehow very important and we dont know why...
        Set<GridVertex> taken = sourceCandidates.stream().filter(GridVertex::isTaken).collect(Collectors.toSet());
        if (taken.isEmpty()) {
            return sourceCandidates;
        }
        Set<GridVertex> takenWithSourceStation = taken.stream().filter(g -> sourceStationName.equals(g.getStationName())).collect(Collectors.toSet());
        if (takenWithSourceStation.isEmpty()) {
            return sourceCandidates;
        }
        return takenWithSourceStation;

    }

    private GraphPath<GridVertex, GridEdge> getShortestPathBetweenTwoSets(Set<GridVertex> sourceCandidates, Set<GridVertex> targetCandidates) {
        double shortestDistance = Double.MAX_VALUE;
        GridVertex finalSource = null;
        GridVertex finalTarget = null;
        GraphPath<GridVertex, GridEdge> shortestPath = null;
        for (GridVertex source: sourceCandidates) {
            for(GridVertex target: targetCandidates) {
                GraphPath<GridVertex, GridEdge> path = DijkstraShortestPath.findPathBetween(gridGraph, source, target);
                if (path == null) {
                    continue;
                }
                double length = getTotalWeight(path.getEdgeList());
                if (length < shortestDistance) {
                    shortestPath = path;
                    shortestDistance = length;
                }
            }
        }
        if (shortestPath == null) {
            logger.warn("No shortest path found!");
            return shortestPath;
        }
        // The path source destination direction can be reversed (because path is undirected I guess)
        List<GridEdge> pathList = shortestPath.getEdgeList();
        if (!shortestPath.getStartVertex().equals(pathList.get(0).getSource())) {
            pathList.get(0).reverse();
        }
        for (int i = 0; i < pathList.size() - 1; i++) {
            GridEdge current = pathList.get(i);
            GridEdge next = pathList.get(i + 1);
            if (!current.getDestination().equals(next.getSource())) {
                next.reverse();
            }
        }

        return shortestPath;
    }

    private double getTotalWeight(List<GridEdge> edgeList) {
        double totalWeight = 0;
        for (GridEdge gridEdge : edgeList) {
            totalWeight += gridEdge.getCosts();
        }
        return totalWeight;
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

    public void reopenSinkEdgesFor(String lineName, List<List<GridEdge>> allPaths) {

        // this is to allow double routing if two lines share the same edge
        List<GridEdge> paths = allPaths.stream().flatMap(Collection::stream).toList();
        for (GridEdge edge: paths) {
            if (edge.isClosedForLine(lineName)) {
                edge.setCostsInf();
            } else {
                edge.resetCosts();
            }
        }
    }

    public void closeSinkEdgesAroundVertices(String lineName, List<List<GridVertex>> allVertices) {
        List<GridVertex> vertices = allVertices.stream().flatMap(Collection::stream).toList();
        vertices.forEach(vertex -> {
            if (vertex.getStationName() != null && vertex.getStationName().equals("Westbahnhof")) {
                int a = 0;
            }
            Set<GridEdge> adjacentEdges = gridGraph.outgoingEdgesOf(vertex);
            if(vertex.isClosedForLine(lineName) || vertex.getStationName() == null) {
                adjacentEdges.forEach(e -> e.setCostsInf());
            } else {
                adjacentEdges.forEach(e -> e.resetCosts());
            }
        });
    }
}
