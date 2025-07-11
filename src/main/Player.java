package main;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Player {
    private int x, y; // Posição atual do jogador (canto superior esquerdo do hitbox)
    private int width = 50; // Largura padrão do jogador
    private int height = 100; // Altura padrão do jogador
    private int currentHeight; // Altura atual (muda ao abaixar)
    
    private int speed = 4; // Velocidade de movimento lateral
    private int jumpSpeed = -125; // Velocidade inicial do pulo (negativo para subir)
    private double gravity = 4.5; // Força da gravidade
    
    private boolean isJumping = false; // Estado: está pulando ou caindo
    private boolean isOnGround = true; // Estado: está no chão
    private boolean isDucking = false; // Estado: está abaixado
    
    private int groundY; // A coordenada Y do chão (base dos pés do jogador)

    private ArrayList<Bullet> bullets;
    private KeyHandler keyHandler;
    private int leftKey, rightKey, upKey, downKey, shootKey; // Renomeadas para clareza

    // Construtor atualizado para receber a posição Y do chão
    public Player(int x, int y, KeyHandler kh, int left, int right, int up, int down, int shoot, int groundY) {
        this.x = x;
        // A posição y inicial deve considerar a altura do personagem para que ele fique no chão
        this.y = groundY - height; 
        this.currentHeight = height; // Inicia com altura padrão

        this.keyHandler = kh;
        this.leftKey = left;
        this.rightKey = right;
        this.upKey = up;
        this.downKey = down;
        this.shootKey = shoot;
        bullets = new ArrayList<>();
        this.groundY = groundY; // Salva a coordenada Y do chão
    }

    public void update() {
        // --- Movimento Lateral ---
        if (keyHandler.isKeyPressed(leftKey)) {
            x -= speed;
        }
        if (keyHandler.isKeyPressed(rightKey)) {
            x += speed;
        }

        // --- Pulo ---
        // Se a tecla de pular for pressionada E o jogador estiver no chão
        if (keyHandler.isKeyPressed(upKey) && isOnGround) {
            isJumping = true;
            isOnGround = false; // Não está mais no chão
            y += jumpSpeed; // Aplica a velocidade inicial do pulo (para cima)
        }

        // Se estiver pulando ou no ar (não no chão), aplica gravidade
        if (!isOnGround) {
            y += gravity; // Aumenta Y (move para baixo)
        }

        // Verifica se o jogador atingiu o chão
        // y + currentHeight é a base (pés) do jogador
        if (y + currentHeight >= groundY) {
            y = groundY - currentHeight; // Garante que ele pare *exatamente* no chão
            isOnGround = true;
            isJumping = false; // Não está mais pulando
        }
        
        // --- Abaixar ---
        // Se a tecla "para baixo" for pressionada e não estiver abaixado
        if (keyHandler.isKeyPressed(downKey) && !isDucking) {
            isDucking = true;
            currentHeight = height / 2; // Reduz a altura pela metade, por exemplo
            y = groundY - currentHeight; // Ajusta a posição Y para a base continuar no chão
        } 
        // Se a tecla "para baixo" for liberada E estiver abaixado
        else if (!keyHandler.isKeyPressed(downKey) && isDucking) {
            isDucking = false;
            currentHeight = height; // Volta para a altura padrão
            y = groundY - currentHeight; // Ajusta a posição Y para a base continuar no chão
        }


        // --- Tiro ---
        if (keyHandler.isKeyPressed(shootKey)) {
            shoot();
        }

        // Atualiza e remove balas
        for (Bullet b : bullets) b.update();
        bullets.removeIf(b -> !b.isVisible());
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        // Desenha o jogador com a altura atual (padrão ou abaixado)
        g.fillRect(x, y, width, currentHeight); 
        
        // Desenha as balas
        for (Bullet b : bullets) b.draw(g);
    }

    // Método de tiro (pode precisar de um cooldown para não atirar continuamente)
    public void shoot() {
        // Exemplo simples de cooldown: só permite um novo tiro se não houver muitas balas na tela
        if (bullets.size() < 5) {
            bullets.add(new Bullet(x + width / 2, y + currentHeight / 2)); // Bala sai do centro do jogador
        }
    }

    // Retorna os limites de colisão do jogador com base na altura atual
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, currentHeight);
    }

    public void checkBullets(Boss boss) {
        bullets.removeIf(b -> {
            if (b.getBounds().intersects(boss.getBounds())) {
                boss.hit();
                return true;
            }
            return false;
        });
    }
}