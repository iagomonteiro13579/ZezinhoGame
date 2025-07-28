package main;

import java.awt.*;

public class Bullet {
    private int x, y;
    private int width;
    private int height;
    private int speed;
    private int direction;
    private int damage;
    private boolean visible = true;

    private int dx = 0;
    private int dy = 0;

    // Construtor usado pelo Player (direction: -1 ou 1)
    public Bullet(int x, int y, int width, int height, int speed, int direction, int damage) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.direction = direction;
        this.damage = damage;
    }

    // Construtor usado pelo Boss (usando Point para evitar conflito de assinaturas)
    public Bullet(int x, int y, int width, int height, Point velocity, int damage) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.dx = velocity.x;
        this.dy = velocity.y;
        this.damage = damage;
        this.direction = 0; // Ignora direction
        this.speed = 0;     // Ignora speed
    }

    public void update() {
        // Movimento da bala
        if (direction != 0) {
            x += speed * direction;
        } else {
            x += dx;
            y += dy;
        }

        // Saiu da tela?
        if (x < -width || x > GamePanel.WIDTH || y < -height || y > GamePanel.HEIGHT) {
            visible = false;
            return;
        }

        // Colisão com boss (bala do player)
        if (direction != 0 && GamePanel.boss != null && getBounds().intersects(GamePanel.boss.getBounds())) {
            GamePanel.boss.takeDamage(damage);
            visible = false;
        }

        // Colisão com Player 1 (bala do boss)
        if (direction == 0 && GamePanel.player1 != null && getBounds().intersects(GamePanel.player1.getBounds())) {
            if (!GamePanel.player1.isInvulnerable()) {
                GamePanel.player1.takeDamage(damage);
                // NÃO remover bala para atravessar o player
            }
        }

        // Colisão com Player 2 (bala do boss)
        if (direction == 0 && GamePanel.player2 != null && getBounds().intersects(GamePanel.player2.getBounds())) {
            if (!GamePanel.player2.isInvulnerable()) {
                GamePanel.player2.takeDamage(damage);
                // NÃO remover bala para atravessar o player
            }
        }
    }

    public void draw(Graphics g) {
        if (visible) {
            g.setColor(Color.BLUE);
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
