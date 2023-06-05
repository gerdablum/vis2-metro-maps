package at.tuwien.vis2.metromaps.api.osm;

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
        // we need to switch coordinates bc they are in wrong order :(
        float[] coord = new float[2];
        coord[0] = geometry.coordinates[1];
        coord[1] = geometry.coordinates[0];
        return coord;
    }

    public String getStationName() {
        return properties.name;
    }

    public String getRailwayPosition() {
        return properties.railwayPosition;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Properties {
        private String name;
        @JsonProperty("uic_name")
        private String uicName;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUicName() {
            return uicName;
        }

        public void setUicName(String uicName) {
            this.uicName = uicName;
        }

        public String getRailwayPosition() {
            return railwayPosition;
        }

        public void setRailwayPosition(String railwayPosition) {
            this.railwayPosition = railwayPosition;
        }

        // this has to be string bc the data at station "Längenfeldgasse" looks like this:
        // "5.9 / 6.6"
        @JsonProperty("railway:position")
        private String railwayPosition;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Geometry {
        private float[] coordinates;

        public float[] getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(float[] coordinates) {
            this.coordinates = coordinates;
        }
    }
}

