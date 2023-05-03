package at.tuwien.vis2.metromaps.model;

import org.jgrapht.graph.DefaultEdge;

// TODO this class is to wrap default edge and prevent serializing errors with jackson
public class GridEdge {


    private GridVertex source;
    private GridVertex destination;

    public GridEdge(GridVertex source, GridVertex destination ) {
        this.source = source;

        this.destination = destination;
    }

    public GridVertex getSource() {
        return source;
    }

    public GridVertex getDestination() {
        return destination;
    }
}
