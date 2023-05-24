package at.tuwien.vis2.metromaps.model;

import org.jgrapht.graph.DefaultEdge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class OctalinearGraphCalculator {

    private GridGraph gridGraph;
    private MetroDataProvider metroDataProvider;

    @Autowired
    public OctalinearGraphCalculator(MetroDataProvider metroDataProvider) {
        // TODO get this linenames from metroDataProvider
        this.metroDataProvider = metroDataProvider;
    }

    public List<List<GridEdge>> calculateOutputGraph() {
        InputGraph inputGraph = new InputGraph();
        List<String> lineNamesInVienna = Arrays.asList("1", "2", "3", "4", "6");
        for (String lineName: lineNamesInVienna) {
            List<MetroLineEdge> orderedEdgesForLine = metroDataProvider.getOrderedEdgesForLine(lineName);
            inputGraph.addEdgeAndSourceDestVertices(orderedEdgesForLine);
        }
        inputGraph.calcBoundingBox();
        this.gridGraph = new GridGraph(inputGraph.getWidth(), inputGraph.getHeight(), inputGraph.getLeftUpperCoordinates(),
                inputGraph.getLeftLowerCoordinates(), inputGraph.getRightUpperCoordinates());
        List<MetroLineEdge> edgesSorted = inputGraph.sortEdges();
        List<List<GridEdge>> allPaths = new ArrayList<>();
        for (MetroLineEdge edge : edgesSorted) {
           List<GridEdge> path = gridGraph.processInputEdge(edge, edge.getStartStation(), edge.getEndStation());
            allPaths.add(path);
        }
        return allPaths;
    }

    public GridGraph getGridGraph() {
        return  gridGraph;
    }


}
