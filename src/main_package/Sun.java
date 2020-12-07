package main_package;

import sun.security.jgss.krb5.ServiceCreds;

import java.awt.*;
import java.util.ArrayList;

public class Sun { //экранное солнышко
    //private RealPoint point;
    private ScreenPoint point;  //центр
    private RealSun realSun;
    private int r;  //радиус солнца
    private int R;  //радиус лучиков
    private int n;  //количество лучиков
    private Color c;
    private boolean isChosen;
    private ArrayList<Marker> markers;

    public Sun(ScreenPoint point, /*int r,*/ RealSun realSun) {
        this.point = point;
        this.realSun = realSun;
        this.n = realSun.getN();
        this.r = (int)realSun.getRSun();
        this.R = (int)realSun.getRSun()*2;
        this.c = realSun.getColor();
        this.isChosen = false;
        this.markers = new ArrayList<>();
    }

    public ScreenPoint getPoint() {
        return point;
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

    public RealSun getRealSun() {
        return realSun;
    }

    public void setRealSun(RealSun realSun) {
        this.realSun = realSun;
    }

    public void setMarkers(ArrayList<Marker> markers) {
        this.markers = markers;
    }

    public void setN(int n) {
        this.n = n;
        this.realSun.setN(n);
    }

    public void setRSun(/*int*/double r) {
        this.r = (int)r;
        this.realSun.setRSun(r);
    }

    public void setRRays(/*int*/double R) {
        this.R = (int)R;
        this.realSun.setRRays(R);
    }

    public void setPoint(ScreenPoint point, RealPoint rpoint) {
        this.point = point;
        this.realSun.setPoint(rpoint);
    }

    public void setColor(Color c) {
        this.c = c;
        this.realSun.setColor(c);
    }

    public void setIsChosen(boolean chosen) {
        this.isChosen = chosen;
    }

    public void countMarkers() {
        ArrayList<Marker> markers = new ArrayList<>();
        int r = this.getRSun();
        ScreenPoint center = this.getPoint();
        ScreenPoint p = new ScreenPoint(center.getX() - r, center.getY() - r);//верхняя левая точка
        ScreenPoint p1 = new ScreenPoint(p.getX(), p.getY()); //верхний левый
        ScreenPoint p2 = new ScreenPoint(p.getX()+2*r, p.getY()); //верхнй правый
        ScreenPoint p3 = new ScreenPoint(p.getX()+2*r, p.getY()+2*r); //нижний правый
        ScreenPoint p4 = new ScreenPoint(p.getX(), p.getY()+2*r); //нижний левый
        markers.add(new Marker(p1, 6, MarkerType.TOP_LEFT));
        markers.add(new Marker(p2, 6, MarkerType.TOP_RIGHT));
        markers.add(new Marker(p3, 6, MarkerType.LOWER_RIGHT));
        markers.add(new Marker(p4, 6, MarkerType.LOWER_LEFT));
        this.setMarkers(markers);
    }

}
