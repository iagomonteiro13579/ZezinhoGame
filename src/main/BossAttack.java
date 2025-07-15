// src/main/BossAttack.java
package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
// import java.awt.Graphics2D; // Removido: Não é necessário para o ataque simples

public class BossAttack {
    private int x, y;
    private int width;
    private int height;
    private int damage;
    private boolean visible = true;

    public static final int DEFAULT_HEIGHT = 20; // Altura padrão do feixe de luz

    public BossAttack(int yPosition, int damageValue) {
        this.x = 0; // Começa na borda esquerda da tela
        this.y = yPosition;
        this.width = GamePanel.WIDTH; // Atravessa toda a largura da tela
        this.height = DEFAULT_HEIGHT;
        this.damage = damageValue;
    }

    public void update() {
        // Para um feixe de luz que só aparece e desaparece, não há lógica de movimento contínuo aqui.
        // Ele "existe" por um período ou até colidir.
    }

    public void draw(Graphics g) {
        if (visible) {
            // Graphics2D g2d = (Graphics2D) g; // Removido: Não é necessário para o desenho simples
            g.setColor(Color.YELLOW); // Cor do feixe de luz
            g.fillRect(x, y, width, height); // Desenha o feixe como um retângulo
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

    public int getDamage() {
        return damage;
    }
}