package main;

import java.awt.event.*; // Para KeyEvent, KeyListener
import java.util.HashSet; // Para HashSet
import main.GamePanel; // Importa a classe GamePanel

public class KeyHandler implements KeyListener {
    private HashSet<Integer> keys = new HashSet<>();

    public boolean isKeyPressed(int key) {
        return keys.contains(key);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Menu
        if (GamePanel.gameState == GamePanel.GameState.MENU) {
            switch (key) {
                case KeyEvent.VK_UP:
                    GamePanel.moveMenuSelection(-1);
                    break;
                case KeyEvent.VK_DOWN:
                    GamePanel.moveMenuSelection(1);
                    break;
                case KeyEvent.VK_ENTER:
                    GamePanel.selectMenuOption();
                    break;
                case KeyEvent.VK_ESCAPE:
                    GamePanel.toggleMenu();
                    break;
            }
            return; 
        }

        // Jogo
        keys.add(key);

        if (key == KeyEvent.VK_ESCAPE) {
            GamePanel.toggleMenu();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}