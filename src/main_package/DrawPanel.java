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
        g.setColor(sun.getColor());
        g.fillOval(sc.realToScreen(sun.point).getX(), sc.realToScreen(sun.point).getY(), 2 * sun.getRSun(), 2 * sun.getRSun());

        int x = sc.realToScreen(sun.point).getX() + sun.getRSun();
        int y = sc.realToScreen(sun.point).getY() + sun.getRSun();
        int r = sun.getRSun();
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
            g.setColor(Color.BLACK); //типо контур, но пока фигня какая-то; добавить флаг, чтобы он убирался
            g.drawRect(sc.realToScreen(sun.point).getX(), sc.realToScreen(sun.point).getY(), 2 * sun.getRSun(), 2 * sun.getRSun());
        }
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
                currentSun = new Sun(sc.screenToReal(currP), sc.screenToReal(helpPoint), r);
                suns.add(currentSun);
                currentSun = null;
            }
            repaint();

            if (currentSun != null ) {
                if (flag == 0) {
                    currentSun.setIsChosen(true);
                    flag = 1;
                } else {
                    currentSun.setIsChosen(false);
                    flag = 0;
                }
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

    @Override
    public void mouseDragged(MouseEvent e) {
        ScreenPoint current = new ScreenPoint(e.getX() - 30, e.getY() - 30);
        ScreenPoint helpPoint = new ScreenPoint(e.getX(), e.getY());
            if (currentSun != null) {
                currentSun.setRealPoint(sc.screenToReal(current));
                currentSun.setHelpPoint(sc.screenToReal(helpPoint));
                repaint();
            }

        ScreenPoint curr = new ScreenPoint(e.getX(), e.getY());
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
        if (find(p) == null)
            setCursor(Cursor.getDefaultCursor());
        else {
            if (find(p).isChosen)
                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            else
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
        sc.setW(sc.getW() * scale);
        sc.setH(sc.getH() * scale);
        for (Sun s : suns) {
            s.setRSun(Math.abs(sc.realToScreen(s.point).getX() - sc.realToScreen(s.helpPoint).getX()));
        }
        repaint();
    }



    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            removeSun(suns.get(0));
            //suns.remove(0);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

