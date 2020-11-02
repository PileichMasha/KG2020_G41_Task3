package main_package;

import java.awt.*;
import java.util.ArrayList;

public class Sun {
    //Graphics g;
    int x;
    int y;
    double rx;
    double ry;
    RealPoint point;
    int r;
    int R;
    int n;
    Color c;
    ArrayList<ScreenPoint> screenX;
    ArrayList<ScreenPoint> screenY;

    public Sun(/*Graphics g, */int x, int y, int r, int R, int n, Color c) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.R = R;
        this.n = n;
        this.c = c;
    }

    public Sun(RealPoint p) {
        this.point = p;
        this.rx = p.getX();
        this.ry = p.getY();
        this.r = 30;
        //this.screenX = new ArrayList<>();
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

    public void setN(int n) {
        this.n = n;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setR(int r) {
        this.r = r;
    }

    public static void drawSun(Graphics g, int x, int y, int r, int R, int n, Color c) {
        g.setColor(c);
        g.fillOval(x - r, y - r, 2 * r,2 * r);
    }
}
