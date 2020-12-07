package main_package;

import java.awt.*;

public class Marker {
    //private RealPoint point;
    private ScreenPoint point;
    private int size;
    private MarkerType type;
    private Color c;

    public Marker(ScreenPoint point, int size, MarkerType type) {
        this.point = point;
        this.size = size;
        this.type = type;
        this.c = Color.BLACK;
    }

    /*public Marker(RealPoint point, int size, MarkerType type) {
        this.point = point;
        this.size = size;
        this.type = type;
        this.c = Color.BLACK;
    }*/

    public ScreenPoint getPoint() {
        return point;
    }

    public void setPoint(ScreenPoint point) {
        this.point = point;
    }

    public int getSize() {
        return size;
    }

    public MarkerType getType() {
        return type;
    }

    public void setType(MarkerType type) {
        this.type = type;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Color getC() {
        return c;
    }

    public void setC(Color c) {
        this.c = c;
    }
}
