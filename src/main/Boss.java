package main;

import java.awt.*;
import java.util.ArrayList; // Necessário para a lista de projéteis
import java.util.List;      // Necessário para a lista de projéteis
import java.util.Random;

public class Boss {
    private int x, y;
    private int health = 2200;

    public static final int BOSS_WIDTH = 70;
    public static final int BOSS_HEIGHT = 320;

    private double verticalVelocity = 0;
    private double gravity = 3.5;
    private int jumpSpeed = -30; // Mantido em -30 para pulos visíveis

    private boolean isJumping = false;
    private boolean isOnGround = true;

    private List<Bullet> bossProjectiles; // Reintroduzido: Lista para os projéteis do Boss

    // Variáveis para controlar os disparos
    private boolean hasJumpedAndNotFiredYet = false; // Flag para o tiro no meio do pulo
    private long lastJumpAttackTime = 0; // Para controlar o tempo de disparo no pulo
    private final long JUMP_ATTACK_DELAY_NANOS = 500_000_000L; // 0.5 segundos após o pulo para o disparo

    private int projectilesToFireOnLand = 0; // Contador para os 3 projéteis ao aterrissar
    private long lastLandAttackTime = 0;
    private final long LAND_ATTACK_INTERVAL_NANOS = 300_000_000L; // 0.3 segundos entre cada um dos 3 tiros

    private Random random = new Random();

    public Boss(int x, int y) {
        this.x = x;
        this.y = y;
        this.bossProjectiles = new ArrayList<>(); // Inicializa a lista
    }

    public void update() {
        if (x < 0) x = 0;
        if (x + BOSS_WIDTH > GamePanel.WIDTH) x = GamePanel.WIDTH - BOSS_WIDTH;

        // Lógica de Gravidade e Pulo
        if (!isOnGround) {
            verticalVelocity += gravity;
            y += verticalVelocity;

            // Disparo de UM PROJÉTIL durante o pulo (após uma pequena delay)
            if (isJumping && !hasJumpedAndNotFiredYet && System.nanoTime() - lastJumpAttackTime > JUMP_ATTACK_DELAY_NANOS) {
                fireSingleJumpProjectile();
                hasJumpedAndNotFiredYet = true; // Garante que atire apenas uma vez por pulo
            }

        }

        // Lógica de Aterrissagem
        if (y + BOSS_HEIGHT >= GamePanel.GROUND_Y) {
            y = GamePanel.GROUND_Y - BOSS_HEIGHT;
            isOnGround = true;
            verticalVelocity = 0;

            // Ao tocar o chão, se estava pulando, prepara para os 3 tiros
            if (isJumping) { // Significa que ele acabou de aterrissar de um pulo
                projectilesToFireOnLand = 3; // Prepara para disparar 3 projéteis
                lastLandAttackTime = System.nanoTime(); // Inicia o timer para os tiros no chão
            }
            isJumping = false; // Reseta a flag de pulo
            hasJumpedAndNotFiredYet = false; // Reseta para o próximo pulo
        } else {
            isOnGround = false;
        }

        // Inicia um pulo aleatoriamente quando no chão
        if (isOnGround && random.nextInt(1000) < 5) {
            startJumpAttack();
            lastJumpAttackTime = System.nanoTime(); // Grava o tempo para o tiro do pulo
        }

        // Lógica para disparar os 3 projéteis ao aterrissar
        if (projectilesToFireOnLand > 0 && System.nanoTime() - lastLandAttackTime > LAND_ATTACK_INTERVAL_NANOS) {
            fireLandProjectile();
            projectilesToFireOnLand--;
            lastLandAttackTime = System.nanoTime(); // Reinicia o timer para o próximo tiro
        }


        // Atualiza e remove projéteis do Boss
        for (Bullet bullet : bossProjectiles) {
            bullet.update();
        }
        bossProjectiles.removeIf(bullet -> !bullet.isVisible());
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, BOSS_WIDTH, BOSS_HEIGHT);
        g.setColor(Color.WHITE);
        g.drawString("Boss HP: " + health, x, y - 10);

        // Desenha os projéteis do Boss
        for (Bullet bullet : bossProjectiles) {
            bullet.draw(g);
        }
    }

    private void startJumpAttack() {
        if (isOnGround) {
            verticalVelocity = jumpSpeed;
            isOnGround = false;
            isJumping = true; // Define que o Boss está pulando
            // hasJumpedAndNotFiredYet é setado para false ao aterrisar.
            // lastJumpAttackTime é setado no update principal, antes de chamar aqui.
        }
    }

    // NOVO MÉTODO: Dispara um projétil simples durante o pulo
    private void fireSingleJumpProjectile() {
        // Altura do projétil: No meio do boss, ou em 120px do topo do boss, o que for menor.
        // O valor 120 provavelmente se refere à altura Y do centro do projétil em relação ao Boss.
        // Vamos usar o centro do Boss para simplificar, já que não temos uma "boca" definida.
        // Ou você pode ajustar para y + 120 se quiser que saia de uma altura fixa do topo do boss.
        int projectileY = this.y + (BOSS_HEIGHT / 2); // Meio do boss
        // Limite de 120px de altura para o projétil em relação ao topo do personagem
        // Se for 120 da coordenada Y do Boss, seria this.y + 120. Vamos usar isso.
        // int projectileY = this.y + 120; // Alvo em 120px do topo do Boss

        // Direção: aleatória ou para onde o player estiver? Para "simples", vamos para a esquerda.
        // Se for sempre para a esquerda, direction = -1
        int direction = -1; // Para a esquerda

        // Cria e adiciona o projétil simples (tamanho e velocidade padrão de um tiro do player, por exemplo)
        // Bullet(x, y, width, height, speed, direction, damage)
        bossProjectiles.add(new Bullet(x + BOSS_WIDTH / 2, projectileY, 20, 10, 8, direction, 10));
    }


    // NOVO MÉTODO: Dispara um dos 3 projéteis ao aterrissar
    private void fireLandProjectile() {
        // Altura do projétil: na altura do personagem (do Player), até 120px de y do boss.
        // Considerando que "altura do personagem" se refere ao Y do Player.
        // E "pode ser até 120" como um limite superior para o Y do projétil (ou que o projétil pode vir de Y=120)
        // Para simplificar, vamos atirar da altura fixa Y=120 do Boss, se estiver dentro da área do Boss.
        // Ou da altura do player, se houver um player.
        Player p1 = GamePanel.getInstance().getPlayer1();
        Player p2 = GamePanel.getInstance().getPlayer2();
        Player targetPlayer = (p1 != null) ? p1 : p2; // Pega o player 1 se existir, senão o 2

        int projectileY = this.y + 120; // Atira de 120px da coordenada Y do Boss
        // Ou você pode usar a altura do player se quiser que ele mire na altura do player no chão
        if (targetPlayer != null) {
             projectileY = targetPlayer.getY() + (targetPlayer.getCurrentHeight() / 2);
             // E garantir que não seja menor que um certo valor para não ir pro céu
             if (projectileY < this.y + 100) projectileY = this.y + 100; // Mínimo de 100px do topo do boss
             if (projectileY > this.y + BOSS_HEIGHT - 20) projectileY = this.y + BOSS_HEIGHT - 20; // Máximo
        }


        int direction = -1; // SEMPRE para a esquerda

        // Tamanho maior para os projéteis de aterrissagem, como 50x20
        // Bullet(x, y, width, height, speed, direction, damage)
        bossProjectiles.add(new Bullet(x + BOSS_WIDTH / 2, projectileY, 50, 20, 10, direction, 15));
    }


    public int getHealth() {
        return health;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, BOSS_WIDTH, BOSS_HEIGHT);
    }

    public void hit() {
        health--;
        if (health <= 0) {
            System.out.println("Boss derrotado!");
        }
    }

    // Reintroduzido: Método para verificar colisões dos projéteis do Boss com jogadores
    public void checkBossProjectiles(Player player) {
        bossProjectiles.removeIf(bullet -> {
            if (bullet.getBounds().intersects(player.getBounds())) {
                // Apenas aplica dano se o player estiver no chão (como na versão anterior)
                // Ou você pode remover essa condição se quiser que cause dano no ar também.
                if (player.isOnGround()) {
                    player.takeDamage(bullet.getDamage());
                }
                return true; // Remove o projétil após colidir
            }
            return false; // Mantém o projétil
        });
    }
}