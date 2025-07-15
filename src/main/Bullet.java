package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Bullet {
    private int x, y;
    private int width;
    private int height;
    private int speed;
    private int direction;
    private int damage;
    private boolean visible = true;

    public Bullet(int x, int y, int width, int height, int speed, int direction, int damage) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.direction = direction;
        this.damage = damage;
    }

    public void update() {
        x += speed * direction;
        if (x < -width || x > GamePanel.WIDTH) {
            visible = false;
        }
    }

    public void draw(Graphics g) {
        if (visible) {
            // Projétil do Boss (vermelho) vs. Bala do Player (laranja)
            // Você pode adicionar uma lógica aqui para mudar a cor
            // baseada em quem disparou, se quiser.
            // Por enquanto, o boss pode usar uma cor diferente para seus projéteis.
            if (this.width == 300) { // Uma forma simples de diferenciar os projéteis do boss
                g.setColor(Color.BLUE);
            } else { // Balas do player
                g.setColor(Color.BLUE);
            }
            g.fillRect(x, y, width, height);
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

    public int getX() { return x; }
    public int getY() { return y; }

    public int getDamage() {
        return damage;
    }
}