package at.tuwien.vis2.metromaps.model.input;

/**
 * Represents a metro line with a name and a color
 */
public class InputLine {

    private String name;
    private String color;

    /**
     * Creates a metro line
     * @param name name of the line, e.g. "U4"
     * @param color color of the line in hex or any other string that is readably with css
     */
    public InputLine(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
