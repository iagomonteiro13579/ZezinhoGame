package main;

import java.awt.*;

public class Boss {
    private int x, y; // Posição do chefe
    private int health = 20; // Vida do chefe

    public Boss(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        // Lógica de movimentação do chefe ou outros comportamentos (vazio por enquanto)
        // Você pode adicionar aqui, por exemplo, ele se movendo de cima para baixo
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED); // Cor do chefe
        g.fillRect(x, y, 50, 50); // Desenha o chefe como um quadrado vermelho
        g.setColor(Color.WHITE); // Cor do texto de HP
        g.drawString("Boss HP: " + health, x, y - 10); // Exibe a vida do chefe acima dele
    }

    // Retorna os limites (bounding box) do chefe para detecção de colisão
    public Rectangle getBounds() {
        return new Rectangle(x, y, 50, 50);
    }

    // Método chamado quando o chefe é atingido por uma bala
    public void hit() {
        health--; // Diminui a vida do chefe
        System.out.println("Boss hit! HP: " + health); // Imprime no console
        if (health <= 0) {
            System.out.println("Boss defeated!"); // Chefe derrotado
            // Adicione aqui a lógica para o fim do jogo, vitória, etc.
        }
    }
}