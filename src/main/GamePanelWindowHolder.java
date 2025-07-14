package main;

import javax.swing.JFrame;

public class GamePanelWindowHolder {
    private static JFrame window;

    public static void setWindow(JFrame w) {
        window = w;
    }

    public static JFrame getWindow() {
        return window;
    }
}
