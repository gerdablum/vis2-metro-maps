package at.tuwien.vis2.metromaps.model.grid;

import java.util.List;

/**
 * Wraps a shortest path obtained from Dijkstra Algorithm.
 */
public class ShortestPath {


    private List<GridEdge> edgeList;
    private List<GridVertex> vertexList;

    private GridVertex startVertex;
    private GridVertex endVertex;

    /**
     * Creates a shortest path containing edges and vertices from gridgraph.
     * @param edgeList list of edges. Path is routed along those edges-
     * @param vertexList list of vertices, shortes path touches
     * @param startVertex start of path
     * @param endVertex end of path
     */
    public ShortestPath(List<GridEdge> edgeList, List<GridVertex> vertexList, GridVertex startVertex, GridVertex endVertex) {
        this.edgeList = edgeList;
        this.vertexList = vertexList;
        this.startVertex = startVertex;
        this.endVertex = endVertex;
    }

    public List<GridEdge> getEdgeList() {
        return edgeList;
    }

    public List<GridVertex> getVertexList() {
        return vertexList;
    }

    public GridVertex getStartVertex() {
        return startVertex;
    }

    public GridVertex getEndVertex() {
        return endVertex;
    }
}
