package at.tuwien.vis2.metromaps.model;

import at.tuwien.vis2.metromaps.api.M10Service;
import at.tuwien.vis2.metromaps.model.grid.GridEdge;
import at.tuwien.vis2.metromaps.model.grid.GridGraph;
import at.tuwien.vis2.metromaps.model.grid.GridVertex;
import at.tuwien.vis2.metromaps.model.input.InputGraph;
import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GridGraphTest {

    @Value("classpath:exports/UBAHNOGD_UBAHNHALTOGD.json")
    Resource data;
    @Test
    public void test() {
        GridGraph graph = new GridGraph(2, 2, new double[]{0,0}, new double[]{0,4}, new double[]{4,0});
        System.out.println(graph.getGridVertices().size());
      for( GridVertex vertex : graph.getGridVertices()) {
          System.out.println(Arrays.toString(vertex.getCoordinates()));
      }

        for(GridEdge edge : graph.getEdges()) {
            System.out.println(String.format("From %s to %s", edge.getSource().getName(), edge.getDestination().getName()));
        }

    }

    @Test
    public void overlyComplicatedTestToCheckIfOctilinearVerticesMatchGridVertices() {
        InputGraph inputGraph = new InputGraph();
        InputStation ottakring = new InputStation("Ottakring", "1", new double[]{48.211059149435854,16.311366713192395}, Collections.singletonList("3"));
        InputStation kendlerstrasse =  new InputStation("Kendlerstraße", "2", new double[]{48.204540181864424,16.309147428955267}, Collections.singletonList("3"));
        InputStation huettldorferstr =  new InputStation("Hütteldorfer Straße", "2", new double[]{48.19979659129259,16.311393949424332}, Collections.singletonList("3"));
        InputLineEdge edge1 = new InputLineEdge("1", ottakring, kendlerstrasse, new double[1][0], Collections.singletonList("3"));
        InputLineEdge edge2 = new InputLineEdge("1", kendlerstrasse, huettldorferstr, new double[1][0], Collections.singletonList("3"));
        inputGraph.addEdgeAndSourceDestVertices(Arrays.asList(edge1,edge2));
        inputGraph.calcBoundingBox();
        GridGraph graph = new GridGraph(inputGraph.getWidth(), inputGraph.getHeight(),inputGraph.getLeftUpperCoordinates(),
                inputGraph.getLeftLowerCoordinates(), inputGraph.getRightUpperCoordinates());
        for (InputLineEdge edge : inputGraph.sortEdges()) {
            List<GridEdge> gridEdges = graph.processInputEdge(edge, edge.getStartStation(), edge.getEndStation());
            for(GridEdge e: gridEdges) {
                assertTrue(isGridEdgeSameAsGridVertices(graph, e));
            }
        }
    }

    @Test
    public void overlyComplicatedTestToCheckCoordinatesWithJsonData() {
        MetroDataProvider metroDataProvider = new M10Service(data);
        OctalinearGraphCalculator graphCalculator = new OctalinearGraphCalculator(metroDataProvider);
        List<List<GridEdge>> gridEdgesList = graphCalculator.calculateOutputGraph();
        GridGraph graph = graphCalculator.getGridGraph();
        for (List<GridEdge> gridEdges : gridEdgesList) {
            for (GridEdge e : gridEdges) {
                assertTrue(isGridEdgeSameAsGridVertices(graph, e));
            }
        }
    }

    private boolean isGridEdgeSameAsGridVertices(GridGraph g, GridEdge e) {
        for (GridVertex v : g.getGridVertices()) {
            if (e.getSource().getCoordinates()[0] == v.getCoordinates()[0] &&
                    e.getSource().getCoordinates()[1] == v.getCoordinates()[1]) {
                System.out.println("Source Path coordinates match grid coordinates");
                return true;
            } else if (e.getDestination().getCoordinates()[0] == v.getCoordinates()[0] &&
                    e.getDestination().getCoordinates()[1] == v.getCoordinates()[1]) {
                System.out.println("Target Path coordinates match grid coordinates");
                return true;
            }
        }
        return false;
    }
}