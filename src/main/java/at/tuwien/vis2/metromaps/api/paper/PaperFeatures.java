package at.tuwien.vis2.metromaps.api.paper;

import at.tuwien.vis2.metromaps.model.Utils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaperFeatures {
    static Logger logger = LoggerFactory.getLogger(PaperFeatures.class);

    private Feature[] features;

    public Feature[] getFeatures() {
        return features;
    }

    public void setFeatures(Feature[] features) {
        this.features = features;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private Geometry geometry;
        private Properties properties;

        public String getType() {
            return geometry.type;
        }

        public double[][] getCoordinates() {
            if (geometry.coordinatesLine != null) {
                return geometry.coordinatesLine;
            } else {
                double[][] coords = {geometry.coordinatesStation};
                return coords;
            }
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        public void reverseFromTo() {
            String from = this.properties.getFrom();
            String to = this.properties.getTo();
            this.properties.from = to;
            this.properties.to = from;
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

                ArrayList<Double> coords = (ArrayList<Double>) coordinates;
                double[] convertedCoords = Utils.convertEspg3857ToLatLon(coords.get(0), coords.get(1));
                this.coordinatesStation = new double[] {convertedCoords[1], convertedCoords[0]};

            }  catch (ClassCastException e) {
                try {
                    ArrayList<ArrayList<Double>> coords = (ArrayList<ArrayList<Double>>) coordinates;
                    this.coordinatesLine = coords.stream().map(arr -> {
                        double[] convertedCoords = Utils.convertEspg3857ToLatLon(arr.get(0), arr.get(1));
                        return new double[]{convertedCoords[1], convertedCoords[0]};
                            })
                            .toArray(double[][]::new);
                } catch (ClassCastException ex)  {
                    logger.info("Casting coordinates did not work.", ex);
                }

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
    public static class Properties {
        @JsonProperty("id")
        private String id;
        @JsonProperty("station_id")
        private String stationId;
        @JsonProperty("station_label")
        private String stationLabel;

        private String from;
        private String to;
        private List<Line> lines;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStationId() {
            return stationId;
        }

        public void setStationId(String stationId) {
            this.stationId = stationId;
        }

        public String getStationLabel() {
            return stationLabel;
        }

        public void setStationLabel(String stationLabel) {
            this.stationLabel = stationLabel;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public List<Line> getLines() {
            return lines;
        }

        public void setLines(List<Line> lines) {
            this.lines = lines;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Line {
        private String color;
        private String id;
        private String label;

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

}
