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

       for (Sun s : suns) {
           drawRealSun(gr, s);
       }
       /**/
       g.drawImage(bi, 0, 0, null);
       gr.dispose();
   }

    private void drawLine(LineDrawer ld, Line l) {
       ld.drawLine(sc.realToScreen(l.getP1()), sc.realToScreen(l.getP2()));
    }

    private void drawRealSun(Graphics g, Sun sun) {
        int r = sun.getRSun();
        g.setColor(sun.getColor());
        g.fillOval(sc.realToScreen(sun.point).getX()-r, sc.realToScreen(sun.point).getY()-r, 2 * sun.getRSun(), 2 * sun.getRSun());

        int x = sc.realToScreen(sun.point).getX()-r + sun.getRSun();
        int y = sc.realToScreen(sun.point).getY()-r + sun.getRSun();

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
        if (sun.isChosen){
            g.setColor(Color.BLACK); //типо контур, но пока ерунда какая-то
            g.drawRect(sc.realToScreen(sun.point).getX()-r, sc.realToScreen(sun.point).getY()-r, 2*sun.getRSun(), 2*sun.getRSun());
            drawMarkers(g, sun);
        }
    }
    private void drawMarkers(Graphics g, Sun sun) {
        sun.setMarkers(countMarkers(sun));
        for (Marker m : sun.getMarkers()) {
            drawMarker(g, m);
        }
    }
    private ArrayList<Marker> countMarkers(Sun sun) {
        ArrayList<Marker> markers = new ArrayList<>();
        int r = sun.getRSun();
        ScreenPoint center = sc.realToScreen(sun.point);
        ScreenPoint p = new ScreenPoint(center.getX() - r, center.getY() - r);//верхняя левая точка
        RealPoint p1 = sc.screenToReal(new ScreenPoint(p.getX(), p.getY())); //верхний левый
        RealPoint p2 = sc.screenToReal(new ScreenPoint(p.getX()+2*r, p.getY())); //верхнй правый
        RealPoint p3 = sc.screenToReal(new ScreenPoint(p.getX()+2*r, p.getY()+2*r)); //нижний правый
        RealPoint p4 = sc.screenToReal(new ScreenPoint(p.getX(), p.getY()+2*r)); //нижний левый
        markers.add(new Marker(p1, 6));
        markers.add(new Marker(p2, 6));
        markers.add(new Marker(p3, 6));
        markers.add(new Marker(p4, 6));
        return markers;
    }
    private void drawMarker(Graphics g, Marker m) {
        g.setColor(m.c);
        ScreenPoint mp = sc.realToScreen(m.getPoint());
        ScreenPoint p = new ScreenPoint(mp.getX() - 3, mp.getY() - 3);
        g.fillRect(p.getX(), p.getY(), m.getSize(), m.getSize());
    }


    private ScreenPoint prevDrag;
    private Line currentLine = null;
    private Sun currentSun = null;

    @Override
    public void mouseClicked(MouseEvent e) {
        ScreenPoint currSc = new ScreenPoint(e.getX(), e.getY());
        if (e.getClickCount() == 2) {
            currentSun = find(currSc);
            if (currentSun != null) {
                removeSun(currentSun);
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            currentSun = find(currSc);
            currentSun.setColor(Color.BLUE);
        }
    }

    private Sun find(ScreenPoint p) {
        for (Sun s : suns) {
            int r = s.getRSun();
            ScreenPoint sunScP = sc.realToScreen(s.point);
            if (p.getX() >= sunScP.getX()-2*r-4 && p.getX() <= sunScP.getX()+2*r+4 &&
                p.getY() >= sunScP.getY()-2*r-4 && p.getY() <= sunScP.getY()+2*r+4)
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
            ScreenPoint center = sc.realToScreen(sunToResize.point);
            ScreenPoint sunP = new ScreenPoint(center.getX() - r, center.getY() - r);
                if (sunToResize.isChosen) {
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
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        return false;
    }

    private int flag = 0;
    @Override
    public void mousePressed(MouseEvent e) {
        //currentSun = new Sun(e.getX() - 60, e.getY() - 60, 30, 30, 12, Color.ORANGE);
        if (e.getButton() == MouseEvent.BUTTON1) {
            ScreenPoint currSc = new ScreenPoint(e.getX(), e.getY());
            currentSun = find(currSc);
            if (currentSun == null) {
                ScreenPoint currP = new ScreenPoint(e.getX() - 30, e.getY() - 30);  //30 - радиус, поменять потом
                ScreenPoint helpPoint = new ScreenPoint(e.getX(), e.getY());
                int r = Math.abs(helpPoint.getX() - currP.getX());
                currentSun = new Sun(sc.screenToReal(helpPoint), sc.screenToReal(currP), 30);
                currentSun.setMarkers(countMarkers(currentSun));
                suns.add(currentSun);
                currentSun = null;
            }
            repaint();

            if (currentSun != null ) {
                if (flag == 0) {
                    currentSun.setIsChosen(true);
                    flag = 1;
                } /*else {
                    currentSun.setIsChosen(false);
                    flag = 0;
                }*/
            }
        }
//придумать нормально, какая кнопка за что отвечает
        if (e.getButton() == MouseEvent.BUTTON3) {
            prevDrag = new ScreenPoint(e.getX(), e.getY());
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
            lines.add(currentLine);
            currentLine = null;
        }
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
                ScreenPoint mPoint = sc.realToScreen(m.getPoint());
                if (p.getX() >= mPoint.getX()-4 && p.getX() <= mPoint.getX()+4 &&
                        p.getY() >= mPoint.getY()-4 && p.getY() <= mPoint.getY()+4)
                    return m;
            }
        }
        return null;
    }
    private int defineMarker(Sun sun, Marker marker) {
        return sun.getMarkers().indexOf(marker);
    }
    private void defineRadius(Sun sun, ScreenPoint delta, Marker marker) {
        int n = defineMarker(sun, marker);
        int deltaR = (int)Math.sqrt(delta.getX()*delta.getX() + delta.getY()*delta.getY());//уменьшить в 2 раза ?
        if (n == 0) {
            if (delta.getX() <= 0 && delta.getY() <= 0)
                sun.setRSun(sun.getRSun() + deltaR);
            else if (delta.getX() >= 0 && delta.getY() >= 0)
                sun.setRSun(sun.getRSun() - deltaR);
        } else if (n == 1) {
            if (delta.getX() >= 0 && delta.getY() <= 0)
                sun.setRSun(sun.getRSun() + deltaR);
            else if (delta.getX() <= 0 && delta.getY() >= 0)
                sun.setRSun(sun.getRSun() - deltaR);
        } else if (n == 2) {
            if (delta.getX() >= 0 && delta.getY() >= 0)
                sun.setRSun(sun.getRSun() + deltaR);
            else if (delta.getX() <= 0 && delta.getY() <= 0)
                sun.setRSun(sun.getRSun() - deltaR);
        } else if (n == 3) {
            if (delta.getX() <= 0 && delta.getY() >= 0)
                sun.setRSun(sun.getRSun() + deltaR);
            else if (delta.getX() >= 0 && delta.getY() <= 0)
                sun.setRSun(sun.getRSun() - deltaR);
        }
        sun.setRRays(2*sun.getRSun());
    }

    private void defineCenter(Sun sun, ScreenPoint delta, RealPoint vector, int n) { //n - номер маркера
        RealPoint sunp = sunForResize.point;
        if (n == 0) {
           sun.setRealPoint(new RealPoint(sunp.getX()-vector.getX(), sunp.getY()-vector.getY()));
        } else if (n == 1) {
            sun.setRealPoint(new RealPoint(sunp.getX()+vector.getX(), sunp.getY()-vector.getY()));
        } else if (n == 2) {
            sun.setRealPoint(new RealPoint(sunp.getX()+vector.getX(), sunp.getY()+vector.getY()));
        } else if (n == 3) {
            sun.setRealPoint(new RealPoint(sunp.getX()-vector.getX(), sunp.getY()+vector.getY()));
        }
    }
    /*
    if (n == 0) {
            if (delta.getX() <= 0 && delta.getY() <= 0)

            else if (delta.getX() >= 0 && delta.getY() >= 0)
                return new RealPoint(-vector.getX(), -vector.getY());
        } else if (n == 1) {
            if (delta.getX() >= 0 && delta.getY() <= 0)
                return new RealPoint(vector.getX(), vector.getY());
            else if (delta.getX() <= 0 && delta.getY() >= 0)
                return new RealPoint(-vector.getX(), -vector.getY());
        } else if (n == 2) {
            if (delta.getX() >= 0 && delta.getY() >= 0)
                return new RealPoint(vector.getX(), vector.getY());
            else if (delta.getX() <= 0 && delta.getY() <= 0)
                return new RealPoint(-vector.getX(), -vector.getY());
        } else if (n == 3) {
            if (delta.getX() <= 0 && delta.getY() >= 0)
                return new RealPoint(vector.getX(), vector.getY());
            else if (delta.getX() >= 0 && delta.getY() <= 0)
                return new RealPoint(-vector.getX(), -vector.getY());
        } */
    /*private void defineDirection(Sun sun, ScreenPoint delta, RealPoint vector, Marker marker) {   //изменяет центральную точку
        RealPoint sunP = sunForResize.point;                  //плохо работает для нижних маркеров
        int n = defineMarker(sun, marker);
        if (n == 0) {
            if (delta.getX() >= 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - vector.getX(), sunP.getY() - vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + vector.getX(), sunP.getY() - vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + vector.getX(), sunP.getY() + vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
            else if (delta.getX() >= 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - vector.getX(), sunP.getY() + vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
        } else if (n == 1) {
            if (delta.getX() >= 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + vector.getX(), sunP.getY() + vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
            else if (delta.getX() >= 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + vector.getX(), sunP.getY() - vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - vector.getX(), sunP.getY() + vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - vector.getX(), sunP.getY() - vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
        } else if (n == 2) {
            if (delta.getX() >= 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + vector.getX(), sunP.getY() + vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
            else if (delta.getX() >= 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + vector.getX(), sunP.getY() - vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - vector.getX(), sunP.getY() - vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - vector.getX(), sunP.getY() + vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
        } else if (n == 3) {
            if (delta.getX() < 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + vector.getX(), sunP.getY() + vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + vector.getX(), sunP.getY() - vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() >= 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - vector.getX(), sunP.getY() - vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() >= 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - vector.getX(), sunP.getY() + vector.getY()));
                marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
        }*/
    private void defineDirection(Sun sun, ScreenPoint delta, RealPoint vector, Marker marker) {   //изменяет центральную точку
        RealPoint sunP = sunForResize.point;                  //плохо работает для нижних маркеров
        int n = defineMarker(sun, marker);
        double dx = vector.getX();
        double dy = vector.getY();
        if (n == 0) {
            if (delta.getX() >= 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - dx, sunP.getY() - dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + dx, sunP.getY() - dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + dx, sunP.getY() + dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
            else if (delta.getX() >= 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - dx, sunP.getY() + dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
        } else if (n == 1) {
            if (delta.getX() >= 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + dx, sunP.getY() + dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
            else if (delta.getX() >= 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + dx, sunP.getY() - dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - dx, sunP.getY() + dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - dx, sunP.getY() - dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
        } else if (n == 2) {
            if (delta.getX() >= 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + dx, sunP.getY() + dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
            else if (delta.getX() >= 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + dx, sunP.getY() - dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - dx, sunP.getY() - dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - dx, sunP.getY() + dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
        } else if (n == 3) {
            if (delta.getX() < 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + dx, sunP.getY() + dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
            else if (delta.getX() < 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() + dx, sunP.getY() - dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() + vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() >= 0 && delta.getY() < 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - dx, sunP.getY() - dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() - vector.getY()));
            }
            else if (delta.getX() >= 0 && delta.getY() >= 0) {
                sun.setRealPoint(new RealPoint(sunP.getX() - dx, sunP.getY() + dy));
                //marker.setPoint(new RealPoint(marker.getPoint().getX() - vector.getX(), marker.getPoint().getY() + vector.getY()));
            }
        }

        /*
        RealPoint vect = defineVector(delta, vector, n);
        sun.setRealPoint(new RealPoint(sunP.getX() + vect.getX(), sunP.getY() + vect.getY()));*/
    }

    private Sun sunForResize;
    private ScreenPoint start = null;
    private ScreenPoint end = null;
    //private ScreenPoint mouse = null;
    @Override
    public void mouseDragged(MouseEvent e) {
        ScreenPoint current = new ScreenPoint(e.getX() - 30, e.getY() - 30);
        ScreenPoint helpPoint = new ScreenPoint(e.getX(), e.getY());
        ScreenPoint curr = new ScreenPoint(e.getX(), e.getY());

        sunForResize = find(curr);
        if (getCursor().getType() == Cursor.CROSSHAIR_CURSOR) {
            ScreenPoint delta = new ScreenPoint(
                    curr.getX() - prevDrag.getX(),
                    curr.getY() - prevDrag.getY());
            Marker marker = findMarker(curr);
            //ScreenPoint mp = sc.realToScreen(marker.getPoint());
            int n = defineMarker(sunForResize, marker);
            ScreenPoint center = sc.realToScreen(sunForResize.point);
            int r = sunForResize.getRSun();
            if (n == 0) {
                start = new ScreenPoint(curr.getX(), curr.getY());
                end = new ScreenPoint(center.getX() + r, center.getY() + r);
            } else if (n == 1) {
                start = new ScreenPoint(center.getX() - r, curr.getY());
                end = new ScreenPoint(curr.getX(), center.getY() + r);
            } else if (n == 2) {
                start = new ScreenPoint(center.getX() - r, center.getY() - r);
                end = new ScreenPoint(curr.getX(), curr.getY());
            } else if (n == 3) {
                start = new ScreenPoint(curr.getX(), center.getY() - r);
                end = new ScreenPoint(center.getX() + r, curr.getY());
            }
        }
        int r1 = Math.abs(end.getX() - start.getX())/2;
        int r2 = Math.abs(end.getY() - start.getY())/2;
        int r = Math.max(r1, r2);
        sunForResize.setRSun(r);
        sunForResize.setRRays(2 * r);

        /*sunForResize = find(curr);
            if (sunForResize != null) {
                if (sunForResize.isChosen) {
                    Marker marker = findMarker(curr);
                    if (marker != null) {
                        ScreenPoint delta = new ScreenPoint(
                                curr.getX() - prevDrag.getX(),
                                curr.getY() - prevDrag.getY());
                        RealPoint deltaReal = sc.screenToReal(delta);
                        RealPoint zeroReal = sc.screenToReal(new ScreenPoint(0, 0));
                        RealPoint vector = new RealPoint(
                                deltaReal.getX() - zeroReal.getX(),
                                deltaReal.getY() - zeroReal.getY());
                    RealPoint sunP = sunForResize.point;
                    RealPoint sunH = sunForResize.helpPoint;
                    /*sunForResize.setRealPoint(new RealPoint(
                            sunP.getX() + 2*vector.getX(),
                            sunP.getY() + 2*vector.getY()));
                       /* sunForResize.setHelpPoint(new RealPoint(
                                sunH.getX() + 2*vector.getX(),
                                sunH.getY() + 2*vector.getY()));
                        /*marker.setPoint(new RealPoint(
                                marker.getPoint().getX() + vector.getX(),
                                marker.getPoint().getY() + vector.getY()));*/
                       /* int n = defineMarker(sunForResize, marker);
                        defineRadius(sunForResize, delta, marker);
                        defineCenter(sunForResize, delta, vector, n);
                        //defineDirection(sunForResize, delta, vector, marker);
                        ScreenPoint s = sc.realToScreen(sunForResize.point);
                        ScreenPoint h = sc.realToScreen(sunForResize.helpPoint);
                        //sunForResize.setRealPoint(new RealPoint(sunP.getX() + vector.getX(), sunP.getY() + vector.getY()));
                        //sunForResize.setRSun(sunForResize.getRSun() + (int)Math.sqrt(delta.getX()*delta.getX() + delta.getY()*delta.getY()));

                        prevDrag = curr;
                    }
                }
            }*/
/*


            if (currentSun != null) {
                currentSun.setRealPoint(sc.screenToReal(current));
                currentSun.setHelpPoint(sc.screenToReal(helpPoint));
                repaint();
            }

        if (prevDrag != null) {
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
        /*if (currentLine != null) {
            currentLine.setP2(sc.screenToReal(current));  //линия следит за мышкой
        }*/
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ScreenPoint p = new ScreenPoint(e.getX(), e.getY());
        if (find(p) == null) {
            setCursor(Cursor.getDefaultCursor());
        }
        if (find(p) != null) {
            //mouse = new ScreenPoint(e.getX(), e.getY());
            if (isMarker(p)) {
                sunForResize = find(p);
                prevDrag = new ScreenPoint(e.getX(), e.getY());
            }
        }

        /*else {
            if (find(p).isChosen){
                isMarker(p);
            }

                //setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            else
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }*/

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int clicks = e.getWheelRotation();
        double scale = 1;
        double coef = clicks > 0 ? 0.9 : 1.1;
        for (int i = 0; i < Math.abs(clicks); i++) {
            scale *= coef;
        }
        sc.setW(sc.getW() * scale);
        sc.setH(sc.getH() * scale);
        for (Sun s : suns) {
            s.setRSun(Math.abs(sc.realToScreen(s.point).getX() - sc.realToScreen(s.helpPoint).getX()));
        }
        repaint();
    }



    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

