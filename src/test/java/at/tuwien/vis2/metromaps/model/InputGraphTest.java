package at.tuwien.vis2.metromaps.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class InputGraphTest {

    @Test
    public void testSimpleInputGraph() {
        Station station1 = new Station("A", "A", new double[2], Collections.singletonList("1"));
        Station station2 = new Station("B", "B", new double[2], Collections.singletonList("1"));
        Station station3 = new Station("C", "C", new double[2], Collections.singletonList("1"));
        Station station4 = new Station("D", "D", new double[2], Collections.singletonList("1"));
        Station station5 = new Station("E", "E", new double[2], Collections.singletonList("1"));
        Edge edge1 = new Edge("", station1, station2, new double[1][2], Collections.singletonList("1"));
        Edge edge2 = new Edge("", station2, station3, new double[1][2], Collections.singletonList("1"));
        Edge edge3 = new Edge("", station4, station3, new double[1][2], Collections.singletonList("1"));
        Edge edge4 = new Edge("", station3, station4, new double[1][2], Collections.singletonList("2"));
        Edge edge5 = new Edge("", station4, station5, new double[1][2], Collections.singletonList("2"));

        InputGraph graph = new InputGraph();

        graph.addVertices(Arrays.asList(edge1, edge2, edge3, edge4, edge5));

        graph.printToCommandLine();

    }

}