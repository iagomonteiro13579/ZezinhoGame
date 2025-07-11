package main;

import java.awt.*;

public class Boss {
    private int x, y; // Posição do chefe
    private int health = 20; // Vida do chefe

    // Definindo as dimensões do Boss como constantes
    // Isso torna mais fácil mudar em um só lugar e garantir consistência
    public static final int BOSS_WIDTH = 70;
    public static final int BOSS_HEIGHT = 320;


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
        g.fillRect(x, y, BOSS_WIDTH, BOSS_HEIGHT); // Desenha o chefe como um retângulo vermelho com as dimensões da constante
        g.setColor(Color.WHITE); // Cor do texto de HP
        g.drawString("Boss HP: " + health, x, y - 10); // Exibe a vida do chefe acima dele
    }

    // Retorna os limites (bounding box) do chefe para detecção de colisão
    public Rectangle getBounds() {
        // --- CORREÇÃO AQUI: Use as mesmas constantes de largura e altura ---
        return new Rectangle(x, y, BOSS_WIDTH, BOSS_HEIGHT);
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