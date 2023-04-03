package at.tuwien.vis2.metromaps.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubwayObject {
    @JsonSetter
    private long id;
    @JsonSetter
    private TYPE type;
    private long[] nodes;
    private Tags tags;
    private float lat;
    private float lon;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Tags {
        private String name;
        private String publicTransport;
        private int railwayTrackRef;
        private String ref;
    }
}
