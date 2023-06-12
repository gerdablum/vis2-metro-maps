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

@Component
public class OctalinearGraphCalculator {

    private MetroDataProvider metroDataProvider;
    Map<String, GridGraph> gridgraphs;
    Map<String, List<List<GridEdge>>> outputGraphs;

    List<List<GridEdge>> allPaths = new ArrayList<>();
    List<List<GridVertex>> allVertices = new ArrayList<>();

    @Autowired
    public OctalinearGraphCalculator(MetroDataProvider metroDataProvider) {
        // TODO get this linenames from metroDataProvider
        this.metroDataProvider = metroDataProvider;
        this.gridgraphs = new HashMap<>();
        this.outputGraphs = new HashMap<>();
        //calculateOutputGraph("Vienna");
    }

    public List<List<GridEdge>> calculateOutputGraph(String city) {
        if (outputGraphs.get(city) != null && !outputGraphs.get(city).isEmpty()) {
            return outputGraphs.get(city);
        }
        InputGraph inputGraph = new InputGraph();
        List<String> allLineNames = metroDataProvider.getAllLineNames(city);
        for (String lineName: allLineNames) {
            List<InputLineEdge> orderedEdgesForLine = metroDataProvider.getOrderedEdgesForLine(lineName, city);
            inputGraph.addEdgeAndSourceDestVertices(orderedEdgesForLine);
        }
        inputGraph.calcBoundingBox();
        GridGraph gridGraph = new GridGraph(inputGraph.getWidth(), inputGraph.getHeight(), inputGraph.getLeftUpperCoordinates(),
                inputGraph.getLeftLowerCoordinates(), inputGraph.getRightUpperCoordinates());
        List<InputLineEdge> edgesSorted = inputGraph.sortEdges();


        //for (String lineName: allLineNames) {
            //gridGraph.closeSinkEdgesAroundVertices(lineName, allVertices);
            //List<InputLineEdge> edgesSorted = metroDataProvider.getOrderedEdgesForLine(lineName, city);
            for (InputLineEdge edge : edgesSorted) {
            gridGraph.reopenSinkEdgesFor("lineName", allVertices);
                ShortestPath path = gridGraph.processInputEdge(edge, edge.getStartStation(), edge.getEndStation(), edge.getLineNames().get(0));
                if (path == null) {
                    continue;
                }
                allPaths.add(path.getEdgeList());
                allVertices.add(path.getVertexList());
            }

        //}
        gridGraph.calculateStationLabelling(allVertices, allPaths);
        gridgraphs.put(city, gridGraph);
        outputGraphs.put(city, allPaths);
        return allPaths;
    }

    public GridGraph getGridGraph(String city) {
        return  gridgraphs.get(city);
    }
}
