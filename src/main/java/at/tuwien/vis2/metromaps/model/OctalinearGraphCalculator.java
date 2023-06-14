package at.tuwien.vis2.metromaps.model;

import at.tuwien.vis2.metromaps.model.grid.GridEdge;
import at.tuwien.vis2.metromaps.model.grid.GridGraph;
import at.tuwien.vis2.metromaps.model.grid.GridVertex;
import at.tuwien.vis2.metromaps.model.grid.ShortestPath;
import at.tuwien.vis2.metromaps.model.input.InputGraph;
import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import org.jgrapht.GraphPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wraps MetroDataProvider, input graph and grid graph and calculates the output graph
 */
@Component
public class OctalinearGraphCalculator {

    private MetroDataProvider metroDataProvider;
    Map<String, GridGraph> gridgraphs;
    Map<String, List<List<GridEdge>>> outputGraphs;

    /**
     * Gets input lines and input stations from MetroDataProvider and creates a grid graph. This class also calculates
     * the octilinear output graph
     * @param metroDataProvider instance that holds all input data
     */
    @Autowired
    public OctalinearGraphCalculator(MetroDataProvider metroDataProvider) {
        this.metroDataProvider = metroDataProvider;
        this.gridgraphs = new HashMap<>();
        this.outputGraphs = new HashMap<>();
        //calculateOutputGraph("Vienna");
    }

    /**
     * calculates the output graph containing all stations and lines from the input graph routed in a way that
     * preserves original station placement as good as possible and minimizes line bend at the same time.
     * First this method gets the ordered input line edges from the metroDataProvider and creates an input graph.
     * This data and the bounding box information is then used to create the grid graph.
     * Next, the input graph sorts all the edges according to the highest ldeg. Then each edge gets processed through
     * the grid graph which returns an optimal path from station to station. All paths are collected and returned.
     * Stores the grid graph for each city in memory. Graph is only recalculated if size or radius changes.
     * @param city name of city chosen
     * @param gridSize distance between each grid vertex
     * @param distanceR search radius for possible source/target vertices when routing optimal paths.
     * @return All routed paths (GridEdges) through the grid graph.
     */
    public List<List<GridEdge>> calculateOutputGraph(String city, double gridSize, double distanceR) {
        if (outputGraphs.get(city) != null && !outputGraphs.get(city).isEmpty() && gridgraphs.get(city).checkGridParameters(gridSize, distanceR)) {
            return outputGraphs.get(city);
        }
        outputGraphs.remove(city);
        gridgraphs.remove(city);
        List<List<GridVertex>> allVertices = new ArrayList<>();
        List<List<GridEdge>> allPaths = new ArrayList<>();
        InputGraph inputGraph = new InputGraph();

        List<String> allLineNames = metroDataProvider.getAllLineNames(city);
        for (String lineName: allLineNames) {
            List<InputLineEdge> orderedEdgesForLine = metroDataProvider.getOrderedEdgesForLine(lineName, city);
            inputGraph.addEdgeAndSourceDestVertices(orderedEdgesForLine);
        }
        inputGraph.calcBoundingBox();
        GridGraph gridGraph = new GridGraph(inputGraph.getWidth(), inputGraph.getHeight(), inputGraph.getLeftUpperCoordinates(),
                inputGraph.getLeftLowerCoordinates(), inputGraph.getRightUpperCoordinates(), gridSize, distanceR);
        //List<InputLineEdge> edgesSorted = inputGraph.sortEdges();

        for (String lineName: allLineNames) {
            gridGraph.reopenSinkEdgesFor(lineName, allVertices);
            //gridGraph.closeSinkEdgesAroundVertices(lineName, allVertices);
            List<InputLineEdge> edgesSorted = metroDataProvider.getOrderedEdgesForLine(lineName, city);
            for (InputLineEdge edge : edgesSorted) {
                ShortestPath path = gridGraph.processInputEdge(edge, edge.getStartStation(), edge.getEndStation(), lineName);
                if (path == null) {
                    continue;
                }
                allPaths.add(path.getEdgeList());
                allVertices.add(path.getVertexList());
            }

        }
        gridGraph.calculateStationLabelling(allVertices, allPaths);
        gridgraphs.put(city, gridGraph);
        outputGraphs.put(city, allPaths);
        return allPaths;
    }

    /**
     *
     * @param city selected city
     * @return gridgraph at the current state of processing
     */
    public GridGraph getGridGraph(String city) {
        return gridgraphs.get(city);
    }
}
