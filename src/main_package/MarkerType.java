package main_package;

public enum MarkerType {
    TOP_LEFT("Top Left"),
    TOP_RIGHT("Top Right"),
    LOWER_RIGHT("Lower Right"),
    LOWER_LEFT("Lower Left");

    private String type;

    MarkerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
