package at.tuwien.vis2.metromaps.api.m10;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * class for old data resource, should not be used
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public class M10Features {
    static Logger logger = LoggerFactory.getLogger(M10Features.class);

    private Feature[] features;

    public Feature[] getFeatures() {
        return features;
    }

    public void setFeatures(Feature[] features) {
        this.features = features;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private String id;
        private Geometry geometry;
        private Properties properties;

        public String getType() {
            return geometry.type;
        }

        public String getId() {
            return id;
        }

        public double[][] getCoordinates() {
            if (geometry.coordinatesLine != null) {
                return geometry.coordinatesLine;
            } else {
                double[][] coords = {geometry.coordinatesStation};
                return coords;
            }
        }

        public String getStationName() {
            return properties.name;
        }

        public int getLineName() {
            return properties.line;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Geometry {
        private String type;
        private double[] coordinatesStation;
        private double[][] coordinatesLine;

        // this ugly method is necessary because depending on the type we either have
        // a one dimesional array (station) or a two dimesional array (line) but same
        // variable name
        public void setCoordinates(Object coordinates) {
            try {
                if (type.equals("Point")) {
                    ArrayList<Double> coords = (ArrayList<Double>) coordinates;
                    double lat = coords.get(1);
                    double lon = coords.get(0);
                    this.coordinatesStation = new double[] {lat, lon};
                }
                else if (type.equals("LineString")) {
                    ArrayList<ArrayList<Double>> coords = (ArrayList<ArrayList<Double>>) coordinates;
                    this.coordinatesLine = coords.stream().map(arr -> new double[]{arr.get(1), arr.get(0)})
                            .toArray(double[][]::new);
                }

            }  catch (ClassCastException e) {
                logger.info("Casting coordinates did not work.", e);
            }

        }

//        public void setCoordinates(float[][] coordinates) {
//            this.coordinatesLine = coordinates;
//        }

        public void setType(String type) {
            this.type = type;
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Properties {
        @JsonProperty("LINFO")
        private int line;
        @JsonProperty("EROEFFNUNG_JAHR")
        private int year;
        @JsonProperty("HTXT")
        private String name;
        @JsonProperty("OBJECTID")
        private String id;

        public void setLine(int line) {
            this.line = line;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
