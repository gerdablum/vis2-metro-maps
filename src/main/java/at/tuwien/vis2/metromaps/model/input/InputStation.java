package at.tuwien.vis2.metromaps.model.input;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a station on the input graph
 */
public class InputStation {

    enum ProcessingState {
        UNPROCESSED,
        PROCESSED,
        DANGLING
    }

    // actually the name is currently primary key, id is not really necessary at this point
    private String name;
    private String id;
    private double[] coordinates;
    private List<InputLine> line;
    private ProcessingState processingState;

    /**
     * Creates an input station
     * @param name name of the station (e.g. Schlachthausgasse). Must be unique
     * @param id
     * @param coordinates location of the station
     * @param line all lines that pass through this station (Number of lines -> ldeg)
     */
    public InputStation(String name, String id, double[] coordinates, List<InputLine> line) {
        if (name == null) {
            this.name = id;
        } else {
            this.name = name;
        }
        this.id = id;
        this.coordinates = coordinates;
        this.line = line;
        this.processingState = ProcessingState.UNPROCESSED;
    }

    /**
     *
     * @return unique station name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return id of station
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return location of station
     */
    public double[] getCoordinates() {
        return coordinates;
    }

    /**
     *
     * @return all lines travelling through this station
     */
    public List<InputLine> getLine() {
        return line;
    }

    ProcessingState getProcessingState() {
        return processingState;
    }

    void setProcessingState(ProcessingState processingState) {
        this.processingState = processingState;
    }

    /**
     * Calculates the distance in km from this vertex to a second location
     * @param coordinatesRef coordinates of the second location
     * @return distance in km
     */
    public double getDistanceInKmTo(double[] coordinatesRef) {
        double latRefRadian = Math.toRadians(coordinatesRef[0]);
        double lonRefRadian = Math.toRadians(coordinatesRef[1]);
        double latRadian = Math.toRadians(coordinates[0]);
        double lonRadian = Math.toRadians(coordinates[1]);
        double dlon = lonRadian - lonRefRadian;
        double dlat = latRadian - latRefRadian;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(latRefRadian) * Math.cos(latRadian)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));
        // Radius of earth in kilometers
        double r = 6371;
        return c * r;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InputStation) {
            return this.getName().equals(((InputStation) obj).getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.getId() + ")";
    }

    public void addLines(List<InputLine> lines) {
        if (this.line == null) {
            this.line = new ArrayList<>(lines);
        }
        for (InputLine line: lines) {
            if (!this.line.contains(line)) {
                this.line.add(line);
            }
        }

    }
}
