package main;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int x, y;
    private int width = 80;
    private int height = 160;
    private int currentHeight;

    private int speed = 6;

    // 🔧 Ajuste de física do pulo
    private int jumpSpeed = -60;   // Mais negativo = pulo mais alto
    private double gravity = 3.5;  // Maior = queda mais rápida, menor = queda mais lenta
    private double verticalVelocity = 0;

    private boolean isJumping = false; // Esta variável pode ser substituída por !isOnGround
    private boolean isOnGround = true;
    private boolean isDucking = false;

    private boolean fallThroughPlatform = false; // Permite queda atravessando plataforma (DOWN + JUMP)

    private int groundY;

    private ArrayList<Bullet> bullets;
    private KeyHandler keyHandler;
    private int leftKey, rightKey, upKey, downKey, shootKey;

    // --- Variáveis para o sistema de tiro constante ---
    private long lastShotTime = 0; // Tempo em nanossegundos do último tiro
    // 0.1 segundos em nanossegundos (0.1 * 1_000_000_000)
    private final long shotIntervalNanos = 100_000_000L; 
    // --------------------------------------------------

    public Player(int x, int y, KeyHandler kh, int left, int right, int up, int down, int shoot, int groundY) {
        this.x = x;
        this.y = groundY - height;
        this.currentHeight = height;

        this.keyHandler = kh;
        this.leftKey = left;
        this.rightKey = right;
        this.upKey = up;
        this.downKey = down;
        this.shootKey = shoot;
        bullets = new ArrayList<>();
        this.groundY = groundY;
    }

    public void update() {
        // Movimento lateral
        if (keyHandler.isKeyPressed(leftKey)) x -= speed;
        if (keyHandler.isKeyPressed(rightKey)) x += speed;

        // Detecta comando de atravessar plataforma (DOWN_KEY + UP_KEY)
        fallThroughPlatform = keyHandler.isKeyPressed(downKey) && keyHandler.isKeyPressed(upKey);

        // Pulo normal
        if (keyHandler.isKeyPressed(upKey) && isOnGround && !fallThroughPlatform) {
            verticalVelocity = jumpSpeed;
            isOnGround = false;
        }

        // Gravidade
        if (!isOnGround) {
            verticalVelocity += gravity;
            y += verticalVelocity;
        }

        // Abaixar
        boolean wasDucking = isDucking;
        if (keyHandler.isKeyPressed(downKey) && !isDucking) {
            isDucking = true;
            y += (height - (height / 2)); // Move o jogador para baixo para o topo não se mover
            currentHeight = height / 2;
        } else if (!keyHandler.isKeyPressed(downKey) && isDucking) {
            isDucking = false;
            // Se levantou, ajusta a posição Y para que o chão permaneça o mesmo
            y -= (height - currentHeight); // Corrige a posição para cima quando levanta
            currentHeight = height;
        }

        // Colisão com chão principal
        if (y + currentHeight >= groundY) {
            y = groundY - currentHeight;
            isOnGround = true;
            verticalVelocity = 0;
        } else {
            isOnGround = false; // Se não está no chão principal, pode estar no ar ou em plataforma
        }

        // Colisão com plataformas
        checkPlatformCollision(GamePanel.getInstance().getPlatforms());

        // --- Lógica de Tiro Constante ---
        if (keyHandler.isKeyPressed(shootKey)) {
            long currentTime = System.nanoTime();
            if (currentTime - lastShotTime >= shotIntervalNanos) {
                shoot();
                lastShotTime = currentTime; // Reinicia o timer
            }
        }
        // ----------------------------------

        // Atualiza e remove balas fora da tela
        for (Bullet b : bullets) {
            b.update();
        }
        bullets.removeIf(b -> !b.isVisible());
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, currentHeight);

        // Desenha as balas
        for (Bullet b : bullets) {
            b.draw(g);
        }
    }

    public void shoot() {
        bullets.add(new Bullet(x + width / 2, y + currentHeight / 2, 1)); // Direção: 1 para direita
        // DEBUG: Verifique onde a bala está sendo criada
        // System.out.println("Bullet created at X: " + (x + width / 2) + ", Y: " + (y + currentHeight / 2));
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, currentHeight);
    }

    public void checkBullets(Boss boss) {
        bullets.removeIf(b -> {
            // DEBUG: Verifique a posição da bala e do boss antes de verificar a colisão
            // System.out.println("Bullet position: (" + b.getX() + ", " + b.getY() + ") - Bounds: " + b.getBounds());
            // System.out.println("Boss position: (" + boss.getX() + ", " + boss.getY() + ") - Bounds: " + boss.getBounds());

            if (b.getBounds().intersects(boss.getBounds())) {
                boss.hit();
                // System.out.println("BULLET HIT BOSS!"); // DEBUG: Mensagem de acerto
                return true; // Remove a bala se colidiu
            }
            return false; // Mantém a bala
        });
    }

    public void checkPlatformCollision(List<Platform> platforms) {
        // Guarda a posição Y antes de aplicar a gravidade/movimento neste frame
        // Isso é crucial para detectar colisão "de cima" com plataformas.
        int playerFeetYBeforeMove = y + currentHeight - (int)verticalVelocity; 
        
        Rectangle playerBounds = getBounds();
        boolean landedOnPlatformThisFrame = false;

        for (Platform platform : platforms) {
            Rectangle platformBounds = platform.getBounds();

            // Condições para pousar em uma plataforma:
            // 1. O jogador está caindo (velocidade vertical é para baixo ou zero, mas vindo de cima)
            // 2. A parte inferior do jogador está colidindo ou um pouco abaixo da parte superior da plataforma
            //    (verificando a interseção)
            // 3. A parte inferior do jogador na _próxima_ posição (se a gravidade agisse sem colisão)
            //    estaria abaixo ou na altura da plataforma.
            // 4. O jogador não está tentando cair através da plataforma (fallThroughPlatform)
            // 5. O jogador se sobrepõe horizontalmente com a plataforma.
            
            // Lógica mais robusta para pousar na plataforma:
            if (verticalVelocity >= 0 && playerBounds.intersects(platformBounds) &&
                playerFeetYBeforeMove <= platformBounds.y && // Estava acima ou na linha da plataforma antes de cair
                x + width > platformBounds.x && x < platformBounds.x + platformBounds.width &&
                !fallThroughPlatform) {
                
                y = platformBounds.y - currentHeight; // Posiciona o jogador exatamente em cima da plataforma
                verticalVelocity = 0; // Para a queda
                isOnGround = true; // Agora está "no chão" (na plataforma)
                landedOnPlatformThisFrame = true;
                break; // Sai do loop, pois já achou uma plataforma para pousar
            }
        }

        // Se não pousou em nenhuma plataforma e não está no chão principal, está no ar
        if (!landedOnPlatformThisFrame && y + currentHeight < groundY) {
            isOnGround = false;
        } else if (!landedOnPlatformThisFrame && y + currentHeight >= groundY) {
            // Se não pousou em plataforma, mas atingiu ou passou do chão principal
            y = groundY - currentHeight;
            isOnGround = true;
            verticalVelocity = 0;
        }
    }
}