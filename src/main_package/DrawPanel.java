package main_package;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DrawPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private ArrayList<Line> lines = new ArrayList<>();
    private ScreenConverter sc = new ScreenConverter(-2, 2, 4, 4, 800, 600);
    private Line yAxis = new Line(0, -1, 0, 1);
    private Line xAxis = new Line(-1, 0, 1, 0);
    private ArrayList<Sun> suns = new ArrayList<>();

    public DrawPanel() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
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

       /*Sun miniSun = new Sun( 150,150, 30, 100, 12, Color.ORANGE);
       suns.add(miniSun);*/
       RealPoint rp = new RealPoint(50, 50);
       //drawSun(miniSun);
       for (Sun s : suns) {
           //drawSun(gr, s);
           drawRealSun(gr, s);
       }
       /**/
       g.drawImage(bi, 0, 0, null);
       gr.dispose();
   }

    private void drawLine(LineDrawer ld, Line l) {
       ld.drawLine(sc.realToScreen(l.getP1()), sc.realToScreen(l.getP2()));
    }
    private void drawSunG(Graphics g, int x, int y, int r, int R, int n, Color c) {
        g.setColor(c);
        g.fillOval(x + r, y + r, 2 * r,2 * r);
    }
    private void drawSun(Graphics g, Sun sun) {
        drawSunG(g, sun.getX(), sun.getY(), sun.getRSun(), sun.getRRays(), sun.getN(), sun.getColor());
    }
    private void drawRealSun(Graphics g, Sun sun) {
        g.setColor(Color.ORANGE);
        g.fillOval(sc.realToScreen(sun.point).getX(), sc.realToScreen(sun.point).getY(), 60, 60);
    }


    private ScreenPoint prevDrag;
    private Line currentLine = null;
    private Sun currentSun = null;

    @Override
    public void mouseClicked(MouseEvent e) {
        ScreenPoint currSc = new ScreenPoint(e.getX(), e.getY());
        if (e.getClickCount() == 2) {
            //currentSun = find(e.getPoint().x-60, e.getPoint().y-60, 30,30,12, Color.ORANGE);
            currentSun = find(currSc);
            if (currentSun != null) {
                removeSun(currentSun);
            }
        }
    }

    /*private Sun find(ScreenPoint p) {
        RealPoint currRp = sc.screenToReal(p);
        for (Sun s : suns) {
            if (currRp.getX() >= s.rx - 5 && currRp.getX() <= s.rx + 5 &&
                    currRp.getY() >= s.ry - 5 && currRp.getY() <= s.ry + 5)
                return s;
        }
        return null;
    }*/
    private Sun find(ScreenPoint p) {
        for (Sun s : suns) {
            ScreenPoint sunScP = sc.realToScreen(s.point);
            if (p.getX() >= sunScP.getX()-60 && p.getX() <= sunScP.getX()+60 &&
                p.getY() >= sunScP.getY()-60 && p.getY() <= sunScP.getY()+60)
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

    @Override
    public void mousePressed(MouseEvent e) {
        //currentSun = find(e.getPoint());
        //currentSun = new Sun(e.getX() - 60, e.getY() - 60, 30, 30, 12, Color.ORANGE);
        ScreenPoint currSc = new ScreenPoint(e.getX(), e.getY());
        currentSun = find(currSc);
        if (currentSun == null) {
            ScreenPoint currP = new ScreenPoint(e.getX() - 30, e.getY() - 30);
            currentSun = new Sun(sc.screenToReal(currP));
            suns.add(currentSun);
        }
        repaint();


        /*if (e.getButton() == MouseEvent.BUTTON3) {
            prevDrag = new ScreenPoint(e.getX(), e.getY());
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            currentLine = new Line(sc.screenToReal(new ScreenPoint(e.getX(), e.getY())),   //начальная точка (в момент нажатия)
                                        sc.screenToReal(new ScreenPoint(e.getX(), e.getY())));
        }
        repaint();*/
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

    @Override
    public void mouseDragged(MouseEvent e) {
        ScreenPoint current = new ScreenPoint(e.getX(), e.getY());
        if (prevDrag != null) {
            ScreenPoint delta = new ScreenPoint(current.getX() - prevDrag.getX(),
                    current.getY() - prevDrag.getY());
            RealPoint deltaReal = sc.screenToReal(delta);
            RealPoint zeroReal = sc.screenToReal(new ScreenPoint(0, 0));
            RealPoint vector = new RealPoint(deltaReal.getX() - zeroReal.getX(), deltaReal.getY() - zeroReal.getY());
            sc.setX(sc.getX() - vector.getX());
            sc.setY(sc.getY() - vector.getY());
            prevDrag = current;
        }
        if (currentLine != null) {
            currentLine.setP2(sc.screenToReal(current));  //линия следит за мышкой
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

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
        repaint();
    }
}

