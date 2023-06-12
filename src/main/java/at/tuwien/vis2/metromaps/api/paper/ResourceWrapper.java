package at.tuwien.vis2.metromaps.api.paper;

import at.tuwien.vis2.metromaps.model.input.InputLineEdge;
import at.tuwien.vis2.metromaps.model.input.InputStation;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ResourceWrapper {
    public Resource resource;
    public List<PaperFeatures.Feature> allPoints;
    public List<PaperFeatures.Feature> allLines;
    public Map<String, InputStation> allInputStations;
    public Map<String, InputLineEdge> allInputLineEdges;
    public Map<String, InputStation> mergedStations;


    public ResourceWrapper(Resource resource) {
        this.resource = resource;
        mergedStations = new HashMap<>();
    }
}
