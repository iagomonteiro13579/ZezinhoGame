package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Insets; 

public class Game {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame window = new JFrame();
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setResizable(false); 
                window.setTitle("Cuphead Clone");

                GamePanel gamePanel = new GamePanel();
                window.add(gamePanel);

                GamePanelWindowHolder.setWindow(window);

                window.pack(); 
                
                window.setLocationRelativeTo(null);
                window.setVisible(true);

                gamePanel.requestFocusInWindow();
            }
        });
    }
}