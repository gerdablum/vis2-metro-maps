package at.tuwien.vis2.metromaps.model.grid;

public class GridVertex {

    private String name;
    private int indexX;
    private int indexY;
    private double[] coordinates;
    private String stationName;
    private boolean isTaken = false;

    public GridVertex(String name, int indexX, int indexY, double[] coordinates) {
        this.name = name;
        this.indexX = indexX;
        this.indexY = indexY;
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndexX() {
        return indexX;
    }

    public void setIndexX(int indexX) {
        this.indexX = indexX;
    }

    public int getIndexY() {
        return indexY;
    }

    public void setIndexY(int indexY) {
        this.indexY = indexY;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public String getStationName() {
        return stationName;
    }

    public boolean isTaken() {
        return isTaken;
    }

    public void setTakenWith(String stationName) {
        isTaken = true;
        this.stationName = stationName;
    }

    public void setTaken() {
        isTaken = true;
    }

    public void release() {
        isTaken = false;
        this.stationName = null;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GridVertex) {
            return this.getName().equals(((GridVertex) obj).getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}