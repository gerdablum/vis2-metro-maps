package at.tuwien.vis2.metromaps.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GridGraphTest {

    @Test
    public void test() {
        GridGraph graph = new GridGraph(2, 2, new double[]{0,0}, new double[]{0,4}, new double[]{4,0});
        System.out.println(graph.getGraph().vertexSet().size());
      for( GridVertex vertex : graph.getGraph().vertexSet()) {
          System.out.println(Arrays.toString(vertex.getCoordinates()));
      }
        graph.printToCommandLine();

    }

}