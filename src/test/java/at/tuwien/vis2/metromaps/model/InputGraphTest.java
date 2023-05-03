package at.tuwien.vis2.metromaps.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class InputGraphTest {


    @Test
    public void testSimpleInputGraph() {
        Station station1 = new Station("A", "A", new double[2], Collections.singletonList("1"));
        Station station2 = new Station("B", "B", new double[2], Collections.singletonList("1"));
        Station station3 = new Station("C", "C", new double[2], Collections.singletonList("1"));
        Station station4 = new Station("D", "D", new double[2], Collections.singletonList("1"));
        Station station5 = new Station("E", "E", new double[2], Collections.singletonList("1"));
        MetroLineEdge edge1 = new MetroLineEdge("", station1, station2, new double[1][2], Collections.singletonList("1"));
        MetroLineEdge edge2 = new MetroLineEdge("", station2, station3, new double[1][2], Collections.singletonList("1"));
        MetroLineEdge edge3 = new MetroLineEdge("", station4, station3, new double[1][2], Collections.singletonList("1"));
        MetroLineEdge edge4 = new MetroLineEdge("", station3, station4, new double[1][2], Collections.singletonList("2"));
        MetroLineEdge edge5 = new MetroLineEdge("", station4, station5, new double[1][2], Collections.singletonList("2"));

        InputGraph graph = new InputGraph();

        graph.addVerticesFromEdges(Arrays.asList(edge1, edge2, edge3, edge4, edge5));

        graph.printToCommandLine();
    }

    @Test
    public void testLdegCalculation() {

        Station station1 = new Station("A", "A", new double[2], Collections.singletonList("1") );
        Station station2 = new Station("B", "B", new double[2], Collections.singletonList("1"));
        Station station3 = new Station("C", "C", new double[2],  Collections.singletonList("1"));
        Station station4 = new Station("D", "D", new double[2], Collections.singletonList("1"));
        MetroLineEdge edge1 = new MetroLineEdge("", station1, station2, new double[1][2], Arrays.asList("1", "2", "3", "4"));
        MetroLineEdge edge2 = new MetroLineEdge("", station2, station3, new double[1][2], Collections.singletonList("1"));
        MetroLineEdge edge3 = new MetroLineEdge("", station3, station4, new double[1][2], Arrays.asList("1", "2", "3", "4", "5", "6"));

        InputGraph graph = new InputGraph();

        graph.addVerticesFromEdges(Arrays.asList(edge1, edge2, edge3));

        int ldeg1 = graph.getLdegForStation(station1);
        int ldeg2 = graph.getLdegForStation(station2);
        int ldeg3 = graph.getLdegForStation(station3);
        int ldeg4 = graph.getLdegForStation(station4);

        List<MetroLineEdge> edges = graph.sortEdges();

        System.out.println(ldeg1);
        System.out.println(ldeg2);
        System.out.println(ldeg3);
        System.out.println(ldeg4);

    }
}