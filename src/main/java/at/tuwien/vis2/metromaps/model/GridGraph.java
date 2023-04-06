package at.tuwien.vis2.metromaps.model;

import org.jgrapht.Graph;

public class GridGraph {

    private int d; // threshold (in meters) we would like to have between each station

    private void calcSizeOfGridGraph(int widthInputGraph, int heightInputGraph) {
        int width = widthInputGraph / d;
        int height = heightInputGraph / d;
    }
}
