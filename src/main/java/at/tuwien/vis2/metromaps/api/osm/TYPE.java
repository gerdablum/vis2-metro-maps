package at.tuwien.vis2.metromaps.api.osm;

public enum TYPE {
    NODE("node"),
    WAY("way");

    private final String name;

    private TYPE(String s) {
        name = s;
    }


    @Override
    public String toString() {
        return name;
    }
}
