package at.tuwien.vis2.metromaps.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubwayStation {

    private String id;
    private Properties properties;
    private Geometry geometry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float[] getCoordinates() {
        return geometry.coordinates;
    }

    public String getStationName() {
        return properties.name;
    }

    public String getRailwayPosition() {
        return properties.railwayPosition;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect
    private static class Properties {
        private String name;
        @JsonProperty("uic_name")
        private String uicName;

        // this has to be string bc the data at station "Längenfeldgasse" looks like this:
        // "5.9 / 6.6"
        @JsonProperty("railway:position")
        private String railwayPosition;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect
    private static class Geometry {
        private float[] coordinates;

    }
}

