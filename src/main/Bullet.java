package main;

import java.awt.*;

public class Bullet {
    private int x, y; // Posição da bala
    private int speed = 10; // Velocidade da bala
    private boolean visible = true; // Se a bala ainda está visível/ativa no jogo

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        x += speed; // Move a bala para a direita
        // Se a bala sair da tela, ela não é mais visível
        if (x > GamePanel.WIDTH) {
            visible = false;
        }
    }

    public void draw(Graphics g) {
        if (visible) { // Desenha a bala apenas se ela estiver visível
            g.setColor(Color.YELLOW); // Cor da bala
            g.fillRect(x, y, 10, 5); // Tamanho da bala (retângulo)
        }
    }

    // Retorna os limites (bounding box) da bala para detecção de colisão
    public Rectangle getBounds() {
        return new Rectangle(x, y, 10, 5);
    }

    // Retorna se a bala está visível
    public boolean isVisible() {
        return visible;
    }

    // Define a visibilidade da bala (usado quando ela colide, por exemplo)
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}