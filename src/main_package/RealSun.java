package main_package;

import java.awt.*;
import java.util.ArrayList;

public class RealSun {
     private RealPoint point;
     //private Sun sun;
     private double r;
     private double R;
     private int n;
     private Color c;

    public RealSun(RealPoint point, double r /*ScreenConverter sc*/) {
        this.point = point;
        this.r = r;
        this.R = 2 * r;
        this.c = Color.ORANGE;
        this.n = 20;
    }

    public RealPoint getPoint() {
        return point;
    }

    public void setPoint(RealPoint point) {
        this.point = point;
    }

    public double getRRays() {
        return R;
    }

    public double getRSun() {
        return r;
    }

    public void setRSun(double r) {
        this.r = r;
    }

    public void setRRays(double R) {
        this.R = R;
    }

    public Color getColor() {
        return c;
    }

    public void setColor(Color c) {
        this.c = c;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

}
