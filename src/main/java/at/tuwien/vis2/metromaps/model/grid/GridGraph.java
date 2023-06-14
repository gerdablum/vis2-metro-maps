package at.tuwien.vis2.metromaps.model.grid;

import at.tuwien.vis2.metromaps.model.input.InputLine;
import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;
import at.tuwien.vis2.metromaps.model.Utils;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wraps a graph with a fixed bounding box and an octilinear grid. It is possible to route paths on the grid graph.
 */
public class GridGraph {

    private final Graph<GridVertex, GridEdge> gridGraph;
    private double d = 0.5; // GRID SIZE: threshold (in km) we would like to have between each grid cell
    private double r = 0.77; // DISTANCE: distance between source and target candidates to match input edges onto grid
    private double costM = 0.5;  // move penalty
    private double costH = 1; // hop cost of using a grid edge
    private int numberOfVerticesHorizontal;
    private int numberOfVerticesVertical;

    Logger logger = LoggerFactory.getLogger(GridGraph.class);
    private String currentLineName = "";
    private GridEdge previouslyUsedEdge;

    public Set<GridVertex> sourceCandidates = new HashSet<>();
    public Set<GridVertex> targetCandidates = new HashSet<>();

    /**
     * Created an octilinear grid graph with vertices of type GridVertex and edges of type GridEdge.
     * Each vertex has 8 outgoing edges. The vertices have a distance of d. The size is calculated based on the given
     * corner points.
     * @param widthInputGraph width calculated in the input graph, based on most left and most right station
     * @param heightInputGraph height calculated in the input graph, based on most upper and most lower station
     * @param leftUpper coordinates of left upper grid graph boundary
     * @param leftLower coordinates of left lower grid graph boundary
     * @param rightUpper coordinates of right upper grid graph boundary
     * @param dinput grid distance, meaning the distance vertices have to each other in kms.
     * @param rinput search radius in km, stations around a radius r are considered as source/target candidates when
     *               routing a path.
     */

    public GridGraph(double widthInputGraph, double heightInputGraph, double[] leftUpper, double[] leftLower, double[] rightUpper, double dinput, double rinput) {
        d = dinput;
        r = rinput;

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
            for (int x = 0; x <= numberOfVerticesHorizontal; x++) {

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

    /**
     * Calculates an optimal path on the grid graph from a given line of the input graph. This is always from a station to another station.
     * Optimal means the bends are as small as possible while also the station placement is close to the original station.
     * This method first calculates source and target candidates (edges) within a radius r of the original stations.
     * If target or source are already routed, these stations are taken as start or end. Otherwise, offset costs are
     * calculated for each source/target candidate edge. The closer the edge to the original station, the lower the offset cost.
     * next, an optimal path from each source to each target candidate is calculated using dijkstra.
     * Setting the costs of each routed edge to inf, prevents routing loops.
     * lastly the optimal path is returned.
     * @param edgeFromInputGraph edge that has to be routed on the grid graph
     * @param sourceFromInputGraph start station from input graph
     * @param targetFromInputGraph destination/target station from input graph.
     * @param lineName line that is routed currently
     * @return shortest path with minimal bends and optimal station placement
     * from input source to input destination routed on the grid graph.
     */
    public ShortestPath processInputEdge(InputLineEdge edgeFromInputGraph, InputStation sourceFromInputGraph, InputStation targetFromInputGraph, String lineName) {
        if (sourceFromInputGraph.getName().equals("Seestadt") || targetFromInputGraph.getName().equals("Aspern Nord")) {
            int a = 1;
        }
        if(!currentLineName.equals(lineName)) {
            previouslyUsedEdge = null;
        }
        logger.info("Routing a path from " + sourceFromInputGraph.getName() + " to " + targetFromInputGraph.getName());
        // set costs back & clear Candidates
        for (GridVertex sourceCandidate : sourceCandidates ) {
            UpdateGridGraphCost(gridGraph.outgoingEdgesOf(sourceCandidate), 1);
        }
        for (GridVertex targetCandidate : targetCandidates ) {
            UpdateGridGraphCost(gridGraph.outgoingEdgesOf(targetCandidate), 1);
        }
        sourceCandidates.clear();
        targetCandidates.clear();

        Set<GridVertex> allGridVertices = new HashSet<>(gridGraph.vertexSet());
        GridVertex takenSource = findAlreadyRoutedGridVertex(allGridVertices, sourceFromInputGraph.getName());
        GridVertex takenTarget = findAlreadyRoutedGridVertex(allGridVertices, targetFromInputGraph.getName());
        if (takenSource != null) {
            sourceCandidates.add(takenSource);
            UpdateGridGraphCost(gridGraph.outgoingEdgesOf(takenSource), 1);
            allGridVertices.remove(takenSource);
        }
        if (takenTarget != null) {
            targetCandidates.add(takenTarget);
            allGridVertices.remove(takenTarget);
            UpdateGridGraphCost(gridGraph.outgoingEdgesOf(takenTarget), 1);
        }

        // before calculating new offset costs, reset the old ones
        // if target candidate is fixed, only calculate offset costs for the target candidates
        // if both target and source are fixed, do not calculate any offset costs
        // if neither target nor source are fixed, calculate offset costs both.

        searchCandidatesAndCalculateOffsetcosts(allGridVertices, sourceFromInputGraph, targetFromInputGraph);

        filterCandidates(sourceCandidates, sourceFromInputGraph.getName());
        filterCandidates(targetCandidates, targetFromInputGraph.getName());

        ShortestPath shortestPath = getShortestPathBetweenTwoSets((sourceCandidates), (targetCandidates), previouslyUsedEdge);
        if (shortestPath == null) {
            return null;
        }
        // set already used edges to inf and mark grid edge as taken
        shortestPath.getEdgeList().forEach(gridEdge -> {
            gridEdge.setTaken(edgeFromInputGraph.getLines(), lineName);
            gridEdge.setCostsInf();
            gridGraph.setEdgeWeight(gridEdge, gridEdge.getCosts());
        });
        // update available & taken lines for the vertices
        List<GridVertex> vertexList = shortestPath.getVertexList();
        vertexList.remove(shortestPath.getEndVertex());
        for (GridVertex vertex : vertexList) {
            vertex.setTakenLineNames(sourceFromInputGraph.getLine(), lineName);
            // do not allow loops
            gridGraph.outgoingEdgesOf(vertex).forEach(v -> v.setCostsInf());
        }
        vertexList.add(shortestPath.getEndVertex());
        shortestPath.getEndVertex().setTakenLineNames(targetFromInputGraph.getLine(), lineName);

        currentLineName = lineName;
        previouslyUsedEdge = shortestPath.getEdgeList().get(shortestPath.getEdgeList().size() -1);

        logger.info("routed a path from " + shortestPath.getStartVertex().getName() + " to " +
                shortestPath.getEndVertex().getName() + " with " + shortestPath.getEdgeList().size() + " steps");
        shortestPath.getStartVertex().setTakenWith(sourceFromInputGraph.getName());
        shortestPath.getEndVertex().setTakenWith(targetFromInputGraph.getName());

        setColors(shortestPath.getEdgeList(), edgeFromInputGraph);

        return shortestPath;
    }

    private void filterCandidates(Set<GridVertex> candidates, String inputStationName) {

        Set<GridVertex> cands = new HashSet<>(candidates);
        cands.forEach(v -> {
            if (v.getStationName() != null && !v.getStationName().equals(inputStationName)) {
                candidates.remove(v);
            }
        });
    }

    private GridVertex findAlreadyRoutedGridVertex(Set<GridVertex> gridVertices, String name) {
        Optional<GridVertex> first = gridVertices.stream()
                .filter(v -> v.isTaken() && v.getStationName().equals(name)).findFirst();
        return first.orElse(null);
    }

    private void searchCandidatesAndCalculateOffsetcosts(Set<GridVertex> allGridVertices, InputStation sourceFromInputGraph, InputStation targetFromInputGraph) {
        if(!sourceCandidates.isEmpty() && !targetCandidates.isEmpty()) {
            return;     // if both filled => return (at line crossings)
        }
        double[] sourcePosition = sourceFromInputGraph.getCoordinates();
        double[] targetPosition = targetFromInputGraph.getCoordinates();
        boolean sourceTaken = !sourceCandidates.isEmpty();  // both empty or one filled (2 False or False/True)
        boolean targetTaken = !targetCandidates.isEmpty();

        for (GridVertex vertex : allGridVertices) {// sink and source candidates according to set distance r
            double distanceSource = Utils.getDistanceInKmTo(sourcePosition, vertex.getCoordinates());
            double distanceTarget = Utils.getDistanceInKmTo(targetPosition, vertex.getCoordinates());
                // build voroni diagram to deal with overlapping sink and target candidates

            if (distanceSource < r) {
                if ((distanceSource < distanceTarget && !sourceTaken) || targetTaken) { // empty source
                    sourceCandidates.add(vertex);
                    // set penalty for source
                    Set<GridEdge> edgesCandidates = gridGraph.incomingEdgesOf(vertex);
                    double offsetcosts = calculateDistancePenalty(sourceFromInputGraph.getCoordinates(), vertex.getCoordinates());
                    //logger.info("grid vertex " + vertex.getName() + " with distance " + distanceSource + " gets penalty " + penalty);
                    UpdateGridGraphCost(edgesCandidates, offsetcosts);
                }
            }
            if (distanceTarget < r) {
                if(sourceTaken || (distanceSource >= distanceTarget && !targetTaken)) {
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

    private void setColors(List<GridEdge> gridEdges, InputLineEdge inputEdge) {
        for (GridEdge edge: gridEdges) {
            List<String> colors = inputEdge.getLines().stream().map(InputLine::getColor).toList();
            edge.setColors(colors);
        }
    }

    private void UpdateGridGraphCost(Set<GridEdge> edgesCandidates, double penalty) {
        edgesCandidates.forEach(edge -> {
            edge.updateCosts(penalty);
            //gridGraph.setEdgeWeight(edge, edge.getCosts());
        });
    }

    private ShortestPath getShortestPathBetweenTwoSets(Set<GridVertex> sourceCandidates, Set<GridVertex> targetCandidates, GridEdge previousEdge) {
        double shortestDistance = Double.MAX_VALUE;
        GridVertex finalSource = null;
        GridVertex finalTarget = null;
        ShortestPath shortestPath = null;
        for (GridVertex source: sourceCandidates) {
            for(GridVertex target: targetCandidates) {
                OurDijkstra dijkstra = new OurDijkstra();
                ShortestPath path = dijkstra.findShortestPathBetween(gridGraph, source, target, previousEdge);
                if (path.getEdgeList().isEmpty()) {
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

    /**
     * Proposes a position to place labels in the final octilinear output graph. This method returns the first free position
     * (edge crossing in between stations) for station label placement.
     * @param allVertices all routed vertices on the output graph
     * @param allPaths all routed edges on the output graph
     */
    public void calculateStationLabelling(List<List<GridVertex>> allVertices, List<List<GridEdge>> allPaths) {          // TODO
        Map<String, GridVertex> uniqueVerticesByStation = new HashMap<>();
        for (List<GridVertex> vertices : allVertices) {
            for (GridVertex vertex : vertices) {
                String stationName = vertex.getStationName();
                uniqueVerticesByStation.put(stationName, vertex);
            }
        }
        uniqueVerticesByStation.remove(null);

        int i = uniqueVerticesByStation.size();
        for (Map.Entry<String, GridVertex> entry : uniqueVerticesByStation.entrySet()) {
            String stationName = entry.getKey();
            GridVertex vertex = entry.getValue();
            System.out.println("Station: " + stationName + ", Vertex: " + vertex);


            Set<GridEdge> adjacentEdges = gridGraph.outgoingEdgesOf(vertex);
            int edgeDegree = 1;
            int rotation = 45;
            // 1  2  3
            // 4     5
            // 8  7  6
            for (GridEdge e : adjacentEdges) {// if path not used => place labelling there
                if (!allPaths.stream().anyMatch(path -> path.contains(e))) {
                    if(e.getDestination().getName().equals(vertex.getName())) {
                        e.reverse();
                    }
                    System.out.println("empty edge: " + e.getDestination().getName() + " " + e.getSource().getName());
                    double[] midpointCoords = getMidpointCoordinates(vertex, e.getDestination());
                    double midpointX = midpointCoords[0];
                    double midpointY = midpointCoords[1];
                    System.out.println("Midpoint coordinates: (" + midpointX + ", " + midpointY + ")");
                    vertex.setLabelCoordinates(midpointCoords);
                    switch (edgeDegree) {
                        case 1:
                        case 6:
                            rotation = 45;
                            break;
                        case 2:
                        case 7:
                            rotation = 0;
                            break;
                        case 3:
                        case 8:
                            rotation = -45;
                            break;
                        case 4:
                        case 5:
                            rotation = 0;
                            break;
                    }
                    vertex.setLabelRotation(rotation);
                    break;
                } else {
                    //System.out.println("edge: " + e.getDestination().getName() + " " + e.getSource().getName() + " is in allPaths");
                }
                edgeDegree++;
            }
        }
        System.out.println("Amount: ********** " + i);
    }
    private double[] getMidpointCoordinates(GridVertex centerVertex, GridVertex targetVertex) {
        double centerLatitude = centerVertex.getCoordinates()[0];
        double centerLongitude = centerVertex.getCoordinates()[1];
        double targetLatitude = targetVertex.getCoordinates()[0];
        double targetLongitude = targetVertex.getCoordinates()[1];

        double midpointLatitude = (centerLatitude + targetLatitude) / 2.0;
        double midpointLongitude = (centerLongitude + targetLongitude) / 2.0;

        return new double[] { midpointLatitude, midpointLongitude };
    }


    //    public void calculateStationLabelling() {          // TODO
//        int i =0;
//        for (GridVertex vertex : gridGraph.vertexSet()) {
//            if (vertex.isTaken()) {
//                System.out.println("calculate label position for: " + vertex.getName() + vertex.getStationName());
//                Set<GridEdge> adjacentEdges = gridGraph.outgoingEdgesOf(vertex);
//                adjacentEdges.forEach(e -> {
//                    if (e.getDestination().equals(vertex.getName())) {
//                        //System.out.println(e.getDestination().getName() + vertex.getName());
//                        e.reverse();
//                    }
//                    if (e.isTaken()) {
//                        //System.out.println("edge: " + e.getDestination().getName());
//                    };
//                    System.out.println("edge: " + e.getDestination().getName());
//
//                });
//            }
//        }
//    }
    public void printToCommandLine() {
        Iterator<GridVertex> iter = new DepthFirstIterator<>(gridGraph);
        while (iter.hasNext()) {
            GridVertex vertex = iter.next();
            System.out.println("Vertex " + vertex.getName() );
        }
    }

    /**
     * used for serializing
     * @return
     */
   public Set<GridVertex> getGridVertices() {
        return  gridGraph.vertexSet();
   }

    /**
     * used for serializing
     * @return
     */
   public Set<GridEdge> getEdges() {
        return gridGraph.edgeSet();
   }

    /**
     * reopens already routed edges after a whole line has been routed through the graph.
     * @param lineName line to be opened
     * @param allVertices all vertices from a routed line
     */
    public void reopenSinkEdgesFor(String lineName, List<List<GridVertex>> allVertices) {

        // this is to allow double routing if two lines share the same edge
        List<GridVertex> vertices = allVertices.stream().flatMap(Collection::stream).toList();
        vertices.forEach(v -> {
            Set<GridEdge> gridEdges = gridGraph.outgoingEdgesOf(v);
            gridEdges.forEach( g -> {
                g.resetCosts();
            });
        });
    }

    // TODO delete
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

    /**
     * Check if grid has to be recalculated because input parameters changed
     * @param gridSize distance between each grid vertex
     * @param distanceR search radius for source/target candidates
     * @return false if grid parameters have changed and grid has to be updates, true otherwise.
     */
    public boolean checkGridParameters(double gridSize, double distanceR) {
        if(gridSize == r && distanceR == d) {
            return true;
        }
        return false;
    }
}
