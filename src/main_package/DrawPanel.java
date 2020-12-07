package main_package;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DrawPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    private ArrayList<Line> lines = new ArrayList<>();
    private ScreenConverter sc = new ScreenConverter(-2, 2, 4, 4, 800, 600);
    private Line yAxis = new Line(0, -1, 0, 1);
    private Line xAxis = new Line(-1, 0, 1, 0);
    private ArrayList<Sun> suns = new ArrayList<>();

    public DrawPanel() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.addKeyListener(this);
        //this.setFocusable(true);
        requestFocusInWindow();
    }

    //private //подсветка линии: переводить все линии в экр коорд + знаем экр коорд мыши, можем выичслить расстояние от мыши до линии
    //и если оно +-5px и в границах х, у, то выбираем её (кандидат для редактирования)
    //1 нажатие  выбрали для редактирования, рисуем маркеры - кружочки на концах линии - заменяем значение точки, которую захватили
    //setP изменить на set(x, y) - тогда рисованием и изменение совпадёт; обработка кнопки delete
    //векторы считаем в реальных коорд
    //соотношение сторон в screenConverter

    @Override
    public void paint(Graphics g) {
       BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
       sc.setScreenW(getWidth());
       sc.setScreenH(getHeight());
       Graphics gr = bi.createGraphics();
       gr.setColor(Color.WHITE);
       gr.fillRect(0, 0, getWidth(), getHeight());
       gr.setColor(Color.BLACK);

       PixelDrawer pd = new BufferedImagePixelDrawer(bi);
       LineDrawer ld = new DDALineDrawer(pd);
       /**/
       drawLine(ld, xAxis);
       drawLine(ld, yAxis);
       /*for (Line l : lines)
           ld.drawLine(sc.realToScreen(l.getP1()), sc.realToScreen(l.getP2()));
       if (currentLine != null)
           drawLine(ld, currentLine);*/

        /*for (ScreenSun s : screenSuns) {
            drawScreenSun(gr, s);
        }*/

       for (Sun s : suns) {
           drawSun(gr, s);
       }
       /**/
       g.drawImage(bi, 0, 0, null);
       gr.dispose();
   }

    private void drawLine(LineDrawer ld, Line l) {
       ld.drawLine(sc.realToScreen(l.getP1()), sc.realToScreen(l.getP2()));
    }


    private void drawSun(Graphics g, Sun sun) {
        int r = sun.getRSun();
        g.setColor(sun.getColor());
        g.fillOval(sun.getPoint().getX()-r, sun.getPoint().getY()-r, 2 * sun.getRSun(), 2 * sun.getRSun());

        int x = sun.getPoint().getX()-r + sun.getRSun();
        int y = sun.getPoint().getY()-r + sun.getRSun();

        int R = sun.getRRays();

        double da = 2 * Math.PI / sun.getN();
        for (int i = 0; i < sun.getN(); i++) {
            int dx1, dy1, dx2, dy2;
            dx1 = (int)(x + r * Math.cos(da * i));
            dy1 = (int)(y + r * Math.sin(da * i));
            dx2 = (int)(x + R * Math.cos(da * i));
            dy2 = (int)(y + R * Math.sin(da * i));

            g.drawLine(dx1 , dy1 , dx2 , dy2 );
        }
        if (sun.isChosen()){
            g.setColor(Color.BLACK);
            g.drawRect(sun.getPoint().getX()-r, sun.getPoint().getY()-r, 2*sun.getRSun(), 2*sun.getRSun());
            drawMarkers(g, sun);
        }
    }
    private void drawMarkers(Graphics g, Sun sun) {
        sun.countMarkers();
        for (Marker m : sun.getMarkers()) {
            drawMarker(g, m);
        }
    }
   /* private ArrayList<Marker> countMarkers(Sun sun) {
        ArrayList<Marker> markers = new ArrayList<>();
        int r = sun.getRSun();
        ScreenPoint center = sun.getPoint();
        ScreenPoint p = new ScreenPoint(center.getX() - r, center.getY() - r);//верхняя левая точка
        ScreenPoint p1 = new ScreenPoint(p.getX(), p.getY()); //верхний левый
        ScreenPoint p2 = new ScreenPoint(p.getX()+2*r, p.getY()); //верхнй правый
        ScreenPoint p3 = new ScreenPoint(p.getX()+2*r, p.getY()+2*r); //нижний правый
        ScreenPoint p4 = new ScreenPoint(p.getX(), p.getY()+2*r); //нижний левый
        markers.add(new Marker(p1, 6, MarkerType.TOP_LEFT));
        markers.add(new Marker(p2, 6, MarkerType.TOP_RIGHT));
        markers.add(new Marker(p3, 6, MarkerType.LOWER_RIGHT));
        markers.add(new Marker(p4, 6, MarkerType.LOWER_LEFT));
        return markers;
    }*/
    private void drawMarker(Graphics g, Marker m) {
        g.setColor(m.getC());
        ScreenPoint mp = m.getPoint();
        ScreenPoint p = new ScreenPoint(mp.getX() - 3, mp.getY() - 3);
        g.fillRect(p.getX(), p.getY(), m.getSize(), m.getSize());
    }


    private ScreenPoint prevDrag;
    private Line currentLine = null;
    private Sun currentSun = null;
    private int flag = 0;

    @Override
    public void mouseClicked(MouseEvent e) {
        ScreenPoint currSc = new ScreenPoint(e.getX(), e.getY());
        if (e.getClickCount() == 1) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                currentSun = find(currSc);
                if (currentSun == null) {
                    RealSun real = new RealSun(sc.screenToReal(currSc), 30/*0.2*/);
                    currentSun = new Sun(currSc, real);
                    currentSun.countMarkers();
                    //currentSun.setMarkers(countMarkers(currentSun));

                    suns.add(currentSun);
                    currentSun = null;
                }  else {
                    if (flag == 0) {
                        currentSun.setIsChosen(true);
                        flag = 1;
                    } else {
                        currentSun.setIsChosen(false);
                        flag = 0;
                    }
                }
            }
        }
    }

    private Sun find(ScreenPoint p) {
        for (Sun s : suns) {
            int r = s.getRRays();
            ScreenPoint sunScP = s.getPoint();
            if (p.getX() >= sunScP.getX()-r-4 && p.getX() <= sunScP.getX()+r+4 &&
                p.getY() >= sunScP.getY()-r-4 && p.getY() <= sunScP.getY()+r+4)
                return s;
        }
        return null;
    }
    private void removeSun(Sun s) {
        if (s == currentSun)
            currentSun = null;
        suns.remove(s);
        repaint();
    }
    private boolean isMarker(ScreenPoint p) {
        Sun sunToResize = find(p);
        if (sunToResize != null) {
            int r = sunToResize.getRSun();
            ScreenPoint center = sunToResize.getPoint();
            ScreenPoint sunP = new ScreenPoint(center.getX() - r, center.getY() - r);
                if (sunToResize.isChosen()) {
                    if (p.getX() >= sunP.getX()-4 && p.getX() <= sunP.getX()+4 &&
                        p.getY() >= sunP.getY()-4 && p.getY() <= sunP.getY()+4) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                        return true;
                    } else if (p.getX() >= sunP.getX()+2*r-4 && p.getX() <= sunP.getX()+2*r+4 &&
                               p.getY() >= sunP.getY()-4 && p.getY() <= sunP.getY()+4) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                        return true;
                    } else if (p.getX() >= sunP.getX()-4 && p.getX() <= sunP.getX()+4 &&
                               p.getY() >= sunP.getY()+2*r-4 && p.getY() <= sunP.getY()+2*r+4) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                        return true;
                    } else if (p.getX() >= sunP.getX()+2*r-4 && p.getX() <= sunP.getX()+2*r+4 &&
                               p.getY() >= sunP.getY()+2*r-4 && p.getY() <= sunP.getY()+2*r+4) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                        return true;
                    } else {
                        //setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        return false;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) { //была 3
            prevDrag = new ScreenPoint(e.getX(), e.getY());
            marker = findMarker(new ScreenPoint(e.getX(), e.getY()));
        } /*else if (e.getButton() == MouseEvent.BUTTON1) {
            currentLine = new Line(sc.screenToReal(new ScreenPoint(e.getX(), e.getY())),   //начальная точка (в момент нажатия)
                    sc.screenToReal(new ScreenPoint(e.getX(), e.getY())));
        }*/
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            prevDrag = null;
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            prevDrag = null;
            marker = null;
        }

        /*else if (e.getButton() == MouseEvent.BUTTON1) {
            lines.add(currentLine);
            currentLine = null;
        }*/
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private Marker findMarker(ScreenPoint p) {
        Sun sun = find(p);
        if (sun != null) {
            for (Marker m : sun.getMarkers()) {
                ScreenPoint mPoint = m.getPoint();
                if (p.getX() >= mPoint.getX()-4 && p.getX() <= mPoint.getX()+4 &&
                        p.getY() >= mPoint.getY()-4 && p.getY() <= mPoint.getY()+4)
                    return m;
            }
        }
        return null;
    }

    private Sun sunForResize;
    private ScreenPoint start = null;
    private ScreenPoint end = null;
    private Marker marker = null;
    @Override
    public void mouseDragged(MouseEvent e) {
        ScreenPoint curr = new ScreenPoint(e.getX(), e.getY());

        sunForResize = find(curr);
        if (sunForResize!= null && sunForResize.isChosen()) {
            if (getCursor().getType() == Cursor.CROSSHAIR_CURSOR) {

                //ScreenPoint center = sc.realToScreen(sunForResize.getPoint());
                ScreenPoint center = sunForResize.getPoint();
                int r = (int)sunForResize.getRealSun().getRSun();
                if (marker != null) {
                    if (marker.getType() == MarkerType.TOP_LEFT) {
                        start = new ScreenPoint(curr.getX(), curr.getY());
                        end = new ScreenPoint(center.getX() + r, center.getY() + r);
                    } else if (marker.getType() == MarkerType.TOP_RIGHT) {
                        start = new ScreenPoint(center.getX() - r, curr.getY());
                        end = new ScreenPoint(curr.getX(), center.getY() + r);
                    } else if (marker.getType() == MarkerType.LOWER_RIGHT) {
                        start = new ScreenPoint(center.getX() - r, center.getY() - r);
                        end = new ScreenPoint(curr.getX(), curr.getY());
                    } else if (marker.getType() == MarkerType.LOWER_LEFT) {
                        start = new ScreenPoint(curr.getX(), center.getY() - r);
                        end = new ScreenPoint(center.getX() + r, curr.getY());
                    }
                }
                if (start != null && end != null) {
                    //Marker marker = findMarker(curr);
                    int r1 = Math.abs(end.getX() - start.getX())/2;
                    int r2 = Math.abs(end.getY() - start.getY())/2;
                    int r3 = Math.max(r1, r2);
                    if (marker != null) {
                        if (marker.getType() == MarkerType.TOP_LEFT) {
                            ScreenPoint newCenter = new ScreenPoint((end.getX() - r3) , end.getY() - r3);
                            //sunForResize.setPoint(sc.screenToReal(newCenter));
                            sunForResize.setPoint(newCenter, sc.screenToReal(newCenter));
                        } else if (marker.getType() == MarkerType.TOP_RIGHT) {
                            ScreenPoint newCenter = new ScreenPoint((start.getX() + r3) , end.getY() - r3);
                            sunForResize.setPoint(newCenter, sc.screenToReal(newCenter));
                        } else if (marker.getType() == MarkerType.LOWER_RIGHT) {
                            ScreenPoint newCenter = new ScreenPoint((start.getX() + r3) , start.getY() + r3);
                            sunForResize.setPoint(newCenter, sc.screenToReal(newCenter));
                        } else if (marker.getType() == MarkerType.LOWER_LEFT) {
                            ScreenPoint newCenter = new ScreenPoint((end.getX() - r3) , start.getY() + r3);
                            sunForResize.setPoint(newCenter, sc.screenToReal(newCenter));
                        }
                    }
                    sunForResize.setRSun(r3);
                    sunForResize.setRRays(2 * r3);
                }
            }


        }

        currentSun = find(curr);
        if (!isMarker(curr)) {
            if (currentSun != null && !currentSun.isChosen()) {   //перетаскивание солнышка
                currentSun.setPoint(curr, sc.screenToReal(curr));
                repaint();
            } else if (prevDrag != null && currentSun == null) {                  //перетаскивание всего
                ScreenPoint delta = new ScreenPoint(
                        curr.getX() - prevDrag.getX(),
                        curr.getY() - prevDrag.getY());
                RealPoint deltaReal = sc.screenToReal(delta);
                RealPoint zeroReal = sc.screenToReal(new ScreenPoint(0, 0));
                RealPoint vector = new RealPoint(
                        deltaReal.getX() - zeroReal.getX(),
                        deltaReal.getY() - zeroReal.getY());
                sc.setX(sc.getX() - vector.getX());
                sc.setY(sc.getY() - vector.getY());
                prevDrag = curr;
            }
        }

        /*if (currentLine != null) {
            currentLine.setP2(sc.screenToReal(current));  //линия следит за мышкой
        }*/
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ScreenPoint p = new ScreenPoint(e.getX(), e.getY());
        prevDrag = new ScreenPoint(e.getX(), e.getY());
        if (find(p) != null) {
            if (isMarker(p)) {
                sunForResize = find(p);
            }
        } else {
            //setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int clicks = e.getWheelRotation();
        double scale = 1;
        double coef = clicks > 0 ? 0.9 : 1.1;
        for (int i = 0; i < Math.abs(clicks); i++) {
            scale *= coef;
        }
        ArrayList<RealPoint> centers = new ArrayList<>();
        //ArrayList<RealPoint> helpPoints = new ArrayList<>();
        for (Sun s : suns) {
            centers.add(sc.screenToReal(s.getPoint()));
            /*ScreenPoint c = s.getPoint();
            helpPoints.add(sc.screenToReal(new ScreenPoint(c.getX() - s.getRSun(), c.getY() - s.getRSun())));*/
        }
        sc.setW(sc.getW() * scale);
        sc.setH(sc.getH() * scale);
        for (int i = 0; i < suns.size(); i++) {
            double r = suns.get(i).getRealSun().getRSun() / scale;
            ScreenPoint c = sc.realToScreen(centers.get(i));
            suns.get(i).setPoint(c, sc.screenToReal(c));
            suns.get(i).setRSun(r);
            suns.get(i).setRRays(2 * r);
            /*ScreenPoint c = sc.realToScreen(centers.get(i));
            ScreenPoint h = sc.realToScreen(helpPoints.get(i));
            int r1 = Math.abs(c.getX() - h.getX());
            int r2 = Math.abs(c.getY() - h.getY());
            int r = Math.max(r1, r2);
            suns.get(i).setPoint(c, sc.screenToReal(c));
            suns.get(i).setRSun(r);
            suns.get(i).setRRays(2 * r);*/
        }
        repaint();
    }
/*rcoef = s.getRSun() / oldW;
            int newRadius = (int)(rcoef * sc.getW());*/
/*double oldW = sc.getW();
        double rcoef;*/

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //System.out.println("key " + e.getKeyCode());
        if (currentSun != null) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                removeSun(currentSun);
                currentSun = null;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                    currentSun.setN(currentSun.getN() + 1);
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    currentSun.setN(currentSun.getN() - 1);
            }
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

