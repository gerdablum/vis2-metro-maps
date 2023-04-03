package at.tuwien.vis2.metromaps.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AllSubwayStations {

    private List<SubwayStation> features;

    public List<SubwayStation> getFeatures() {
        return features;
    }

    public void setFeatures(List<SubwayStation> features) {
        this.features = features;
    }
}
