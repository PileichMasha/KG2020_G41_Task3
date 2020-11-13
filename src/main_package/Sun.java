package main_package;

import java.awt.*;
import java.util.ArrayList;

public class Sun {
    private RealPoint point;  //центр
    private int r;  //радиус солнца
    private int R;  //радиус лучиков
    private int n;  //количество лучиков
    private Color c;
    boolean isChosen;
    private ArrayList<Marker> markers;

    public Sun(RealPoint point, int r) {
        this.point = point;
        this.n = 20;
        this.r = r;
        this.R = 2 * r;
        this.c = Color.ORANGE;
        this.isChosen = false;
        this.markers = new ArrayList<>();
    }

    public int getRSun() {
        return r;
    }
    public int getRRays() {
        return R;
    }

    public int getN() {
        return n;
    }

    public Color getColor() {
        return c;
    }

    public boolean isChosen() {
        return isChosen;
    }

    public ArrayList<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(ArrayList<Marker> markers) {
        this.markers = markers;
    }

    public void setN(int n) {
        this.n = n;
    }

    public void setRSun(int r) {
        this.r = r;
    }

    public void setRRays(int R) {
        this.R = R;
    }

    public void setRealPoint(RealPoint point) {
        this.point = point;
    }

    public void setColor(Color c) {
        this.c = c;
    }

    public void setIsChosen(boolean chosen) {
        this.isChosen = chosen;
    }

}
