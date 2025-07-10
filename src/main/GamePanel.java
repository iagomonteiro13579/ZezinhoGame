package main;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int GROUND_Y = 400; // Altura do chão, por exemplo, Y=400. Ajuste conforme necessário.

    private Thread thread;
    private boolean running;

    private Player player1, player2;
    private Boss boss;
    private KeyHandler keyHandler;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.CYAN);
        setDoubleBuffered(true);

        keyHandler = new KeyHandler();
        addKeyListener(keyHandler);

        setFocusable(true);
        
        startGame();
    }

    public void startGame() {
        // Passamos GROUND_Y para o Player agora
        player1 = new Player(100, GROUND_Y, keyHandler, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_F, GROUND_Y);
        player2 = new Player(200, GROUND_Y, keyHandler, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER, GROUND_Y);
        boss = new Boss(600, 100);

        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / 60;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() { // Removida a anotação @Override daqui, conforme corrigido anteriormente
        player1.update();
        player2.update();
        boss.update();

        player1.checkBullets(boss);
        player2.checkBullets(boss);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Opcional: Desenhar uma linha para representar o chão visualmente
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, GROUND_Y + 40, WIDTH, HEIGHT - (GROUND_Y + 40)); // Desenha uma área abaixo do chão
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(0, GROUND_Y + 40, WIDTH, GROUND_Y + 40); // Linha no nível do chão
        // O jogador tem 40 de altura, então o chão real para o *pé* do jogador é GROUND_Y + player.height

        player1.draw(g);
        player2.draw(g);
        boss.draw(g);
    }
}