package main_package;

import javax.swing.*;

public class MainWindow extends JFrame {
    private DrawPanel dp;

    public MainWindow() {
        dp = new DrawPanel();
        this.add(dp);
    }
}
