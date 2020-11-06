package main_package;

import java.awt.*;

public class Marker {
    RealPoint point;
    int size;
    Color c;

    public Marker(RealPoint point, int size) {
        this.point = point;
        this.size = size;
        this.c = Color.BLACK;
    }

    public RealPoint getPoint() {
        return point;
    }

    public void setPoint(RealPoint point) {
        this.point = point;
    }

    public int getSize() {
        return size;
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
