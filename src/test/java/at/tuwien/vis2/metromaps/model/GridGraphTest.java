package at.tuwien.vis2.metromaps.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridGraphTest {

    @Test
    public void test() {
        GridGraph graph = new GridGraph(2, 2);
        System.out.println(graph.getGraph().vertexSet().size());
        graph.printToCommandLine();

    }

}