package at.tuwien.vis2.metromaps.model.grid;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OurDijkstra {

    private Set<GridVertex> settledVertices;
    private Set<GridVertex> unsettledVertices;

    public OurDijkstra() {
        settledVertices = new HashSet<>();
        unsettledVertices = new HashSet<>();
    }

    Logger logger = LoggerFactory.getLogger(OurDijkstra.class);

    public ShortestPath findShortestPathBetween(Graph<GridVertex, GridEdge> gridGraph, GridVertex source, GridVertex target, GridEdge comingFromEdge) {
        setAllDistancesToInfExceptSource(gridGraph, source);
        unsettledVertices.add(source);
        GridEdge previousEdge = comingFromEdge;
        while (unsettledVertices.size() != 0) {
            GridVertex current = getLowestDistanceVertex(unsettledVertices);
            if (current == null) {
                break;
            }
            if (current.equals(target)) {
                break;
            }
            unsettledVertices.remove(current);
            settledVertices.add(current);
            if (current.getShortestPath().size() >= 1) {
                previousEdge = gridGraph.getEdge(current, current.getShortestPath().getLast());
            }

            for (GridEdge adjacentEdge : gridGraph.outgoingEdgesOf(current)) {
                GridVertex adjacentVertex = gridGraph.getEdgeSource(adjacentEdge);
                if (adjacentVertex.equals(current)) {
                    adjacentVertex = gridGraph.getEdgeTarget(adjacentEdge);
                }
                updateBendCosts(previousEdge, adjacentEdge);
                if (!settledVertices.contains(adjacentVertex)) {
                    calculateMinimumDistance(adjacentVertex, adjacentEdge.getCosts(), current);
                    unsettledVertices.add(adjacentVertex);
                }
            }
        }

        LinkedList<GridVertex> vertexList = target.getShortestPath();

        List<GridEdge> edgeList = new ArrayList<>();
        if (vertexList != null) {
            vertexList.add(target);
        }
        for (int i = 0; i < vertexList.size() - 1; i++) {
            GridVertex current = vertexList.get(i);
            GridVertex next = vertexList.get(i+1);
            GridEdge edge = gridGraph.getEdge(current, next);
            edgeList.add(edge);
        }
        return new ShortestPath(edgeList, vertexList, source, target);
    }

    private void calculateMinimumDistance(GridVertex evaluationVertex, double costs, GridVertex sourceVertex) {
        double sourceDistance = sourceVertex.getDistance();
        if (sourceDistance + costs < evaluationVertex.getDistance()) {
            evaluationVertex.setDistance(sourceDistance + costs);
            LinkedList<GridVertex> shortestPath = new LinkedList<>(sourceVertex.getShortestPath());
            shortestPath.add(sourceVertex);
            evaluationVertex.setShortestPath(shortestPath);
        }
    }

    private GridVertex getLowestDistanceVertex(Set<GridVertex> unsettledVertices) {
        GridVertex lowestDistanceVertex = null;
        double lowestDistance = Double.MAX_VALUE;
        for (GridVertex vertex: unsettledVertices) {
            double vertexDistance = vertex.getDistance();
            if (vertexDistance < lowestDistance) {
                lowestDistance = vertexDistance;
                lowestDistanceVertex = vertex;
            }
        }
        return lowestDistanceVertex;
    }

    private void setAllDistancesToInfExceptSource(Graph<GridVertex, GridEdge> gridGraph, GridVertex source) {
        gridGraph.vertexSet().forEach(gridVertex -> {
            gridVertex.setDistance(Double.MAX_VALUE);
            gridVertex.setShortestPath(null);
        });
        source.setDistance(0);
        source.setShortestPath(null);
    }

    public void updateBendCosts(GridEdge incomingEdge, GridEdge outgoingEdge) {

        if (incomingEdge == null) {
            outgoingEdge.updateCosts(GridEdge.BendCost.C_180);
            return;
        }

        if (incomingEdge.equals(outgoingEdge)) {
            return;
        }

        int centerVertexX = incomingEdge.getDestination().getIndexX();
        int centerVertexY = incomingEdge.getDestination().getIndexY();

        int incomingVertexX = incomingEdge.getSource().getIndexX();
        int incomingVertexY = incomingEdge.getSource().getIndexY();

        int incomingEdgeDirectionX = incomingVertexX - centerVertexX;
        int incomingEdgeDirectionY = incomingVertexY - centerVertexY;


        int neighbourCenterX = outgoingEdge.getSource().getIndexX();
        int neighbourCenterY = outgoingEdge.getSource().getIndexY();

        int neighbourOutgoingX = outgoingEdge.getDestination().getIndexX();
        int neighbourOutgoingY = outgoingEdge.getDestination().getIndexY();

        // directions must match!!!
        if (centerVertexX == neighbourOutgoingX && centerVertexY == neighbourOutgoingY) {
            int tempx = neighbourCenterX;
            int tempy = neighbourCenterY;
            neighbourCenterX = neighbourOutgoingX;
            neighbourCenterY = neighbourOutgoingY;
            neighbourOutgoingX = tempx;
            neighbourOutgoingY = tempy;
        }

        int neighbourDirectionX = neighbourOutgoingX - neighbourCenterX;
        int neighbourDirectionY = neighbourOutgoingY - neighbourCenterY;

        if(neighbourCenterX == 21 && neighbourCenterY == 20) {
            int a = 1;
            if(neighbourCenterX == 21 && neighbourCenterY == 20) {
            }
        }

        double angle1 = Math.atan2(-incomingEdgeDirectionY, incomingEdgeDirectionX);
        double angle2 = Math.atan2(-neighbourDirectionY, neighbourDirectionX);
        double angle = Math.toDegrees(angle2 - angle1);
        //logger.info("Edge from vertex " + e.getSource().getName() + " to " +  e.getDestination().getName());
        //logger.info("Setting edge bend cost from " + e.getBendCost() + "  to " + edgeCost);
        if (angle < 0) {
            angle += 360;
        } else if (angle > 360) {
            angle -= 360;
        }
        if (angle > 180) {
            angle = 360 - angle;
        }

        if (angle == 45) {
            outgoingEdge.updateCosts(GridEdge.BendCost.C_45);
        } else if (angle == 90) {
            outgoingEdge.updateCosts(GridEdge.BendCost.C_90);
        } else if (angle == 135) {
            outgoingEdge.updateCosts(GridEdge.BendCost.C_135);
        } else {
            outgoingEdge.updateCosts(GridEdge.BendCost.C_180);
        }
    }
}
