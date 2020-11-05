package main_package;

import java.awt.*;

public class Sun {
    int x;
    int y;
    double rx;
    double ry;
    RealPoint point;
    RealPoint helpPoint;
    int r;
    int R;
    int n;
    Color c;
    boolean isChosen;

    public Sun(/*Graphics g, */int x, int y, int r, int R, int n, Color c) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.R = R;
        this.n = n;
        this.c = c;
    }

    public Sun(RealPoint p, RealPoint helpPoint, int r) {
        this.point = p;
        this.helpPoint = helpPoint;
        this.rx = p.getX();
        this.ry = p.getY();
        this.n = 12;
        this.r = r;
        this.R = 2 * r;
        this.c = Color.ORANGE;
        this.isChosen = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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

    public void setN(int n) {
        this.n = n;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setRSun(int r) {
        this.r = r;
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

    public void setHelpPoint(RealPoint helpPoint) {
        this.helpPoint = helpPoint;
    }

    public void convertSize(double scale) {
        this.setRSun((int)(r / scale));
    }

    public static void drawSun(Graphics g, int x, int y, int r, int R, int n, Color c) {
        g.setColor(c);
        g.fillOval(x - r, y - r, 2 * r,2 * r);
    }
}
