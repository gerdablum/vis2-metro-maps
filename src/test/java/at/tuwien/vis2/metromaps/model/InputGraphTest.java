package at.tuwien.vis2.metromaps.model;

import at.tuwien.vis2.metromaps.model.input.InputGraph;
import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class InputGraphTest {


    @Test
    public void testSimpleInputGraph() {
        InputStation station1 = new InputStation("A", "A", new double[2], Collections.singletonList("1"));
        InputStation station2 = new InputStation("B", "B", new double[2], Collections.singletonList("1"));
        InputStation station3 = new InputStation("C", "C", new double[2], Collections.singletonList("1"));
        InputStation station4 = new InputStation("D", "D", new double[2], Collections.singletonList("1"));
        InputStation station5 = new InputStation("E", "E", new double[2], Collections.singletonList("1"));
        InputLineEdge edge1 = new InputLineEdge("", station1, station2, new double[1][2], Collections.singletonList("1"));
        InputLineEdge edge2 = new InputLineEdge("", station2, station3, new double[1][2], Collections.singletonList("1"));
        InputLineEdge edge3 = new InputLineEdge("", station4, station3, new double[1][2], Collections.singletonList("1"));
        InputLineEdge edge4 = new InputLineEdge("", station3, station4, new double[1][2], Collections.singletonList("2"));
        InputLineEdge edge5 = new InputLineEdge("", station4, station5, new double[1][2], Collections.singletonList("2"));

        InputGraph graph = new InputGraph();

        graph.addEdgeAndSourceDestVertices(Arrays.asList(edge1, edge2, edge3, edge4, edge5));

        graph.printToCommandLine();
    }

    @Test
    public void testLdegCalculation() {

        InputStation station1 = new InputStation("A", "A", new double[2], Collections.singletonList("1") );
        InputStation station2 = new InputStation("B", "B", new double[2], Collections.singletonList("1"));
        InputStation station3 = new InputStation("C", "C", new double[2],  Collections.singletonList("1"));
        InputStation station4 = new InputStation("D", "D", new double[2], Collections.singletonList("1"));
        InputLineEdge edge1 = new InputLineEdge("", station1, station2, new double[1][2], Arrays.asList("1", "2", "3", "4"));
        InputLineEdge edge2 = new InputLineEdge("", station2, station3, new double[1][2], Collections.singletonList("1"));
        InputLineEdge edge3 = new InputLineEdge("", station3, station4, new double[1][2], Arrays.asList("1", "2", "3", "4", "5", "6"));

        InputGraph graph = new InputGraph();

        graph.addEdgeAndSourceDestVertices(Arrays.asList(edge1, edge2, edge3));

        int ldeg1 = graph.getLdegForStation(station1);
        int ldeg2 = graph.getLdegForStation(station2);
        int ldeg3 = graph.getLdegForStation(station3);
        int ldeg4 = graph.getLdegForStation(station4);

        List<InputLineEdge> edges = graph.sortEdges();

        System.out.println(ldeg1);
        System.out.println(ldeg2);
        System.out.println(ldeg3);
        System.out.println(ldeg4);

    }
}