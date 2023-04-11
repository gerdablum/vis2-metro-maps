package at.tuwien.vis2.metromaps.model;

public class GridVertex extends Vertex {

    private String name;
    private int indexX;
    private int indexY;

    public GridVertex(String name, int indexX, int indexY) {
        this.name = name;
        this.indexX = indexX;
        this.indexY = indexY;
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
