package main;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Rectangle;

public class Bullet {
    private int x, y;
    private int width = 20;  // Tamanho do projétil
    private int height = 10;
    private int speed = 15;  // Velocidade do projétil
    private int direction;   // 1 para direita, -1 para esquerda
    private boolean visible = true; // Se a bala ainda está na tela

    public Bullet(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public void update() {
        x += speed * direction; // Move a bala na direção
        // Remove a bala se sair da tela (ajuste os limites conforme GamePanel.WIDTH)
        if (x < -width || x > GamePanel.WIDTH) {
            visible = false;
        }
    }

    public void draw(Graphics g) {
        if (visible) {
            g.setColor(Color.ORANGE); // Cor do projétil
            g.fillOval(x, y, width, height); // Desenha um círculo/oval para o projétil
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}