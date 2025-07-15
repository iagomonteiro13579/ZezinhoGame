package main;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Boss {
    private int x, y;
    private int health = 2200;

    public static final int BOSS_WIDTH = 70;
    public static final int BOSS_HEIGHT = 320;

    private double verticalVelocity = 0;
    private double gravity = 3.5;
    private int jumpSpeed = -30;

    private boolean isJumping = false;
    private boolean isOnGround = true;

    private List<Bullet> bossProjectiles;

    private boolean hasJumpedAndNotFiredYet = false;
    private long lastJumpAttackTime = 0;
    private final long JUMP_ATTACK_DELAY_NANOS = 300_000_000L;

    private int projectilesToFireOnLand = 0;
    private long lastLandAttackTime = 0;
    private final long LAND_ATTACK_INTERVAL_NANOS = 150_000_000L;

    private long lastSpecialAttackTime = 0;
    private final long SPECIAL_ATTACK_COOLDOWN_NANOS = 2_000_000_000L; 

    private boolean isPerformingBulletHell = false;
    private int bulletHellProjectilesFired = 0;
    private final int MAX_BULLET_HELL_PROJECTILES = 7; 
    private final long BULLET_HELL_FIRE_INTERVAL_NANOS = 70_000_000L; 

    // NOVAS VARIÁVEIS PARA O ATAQUE DE INVESTIDA
    private boolean isCharging = false;
    private int chargeDirection = 0; // -1 para esquerda, 1 para direita
    private int chargeSpeed = 15; // Velocidade da investida
    private long lastChargeAttackTime = 0;
    private final long CHARGE_ATTACK_COOLDOWN_NANOS = 4_000_000_000L; // Cooldown de 4 segundos para a investida
    private final int CHARGE_BUFFER_X = 50; // Margem para parar antes da parede

    private Random random = new Random();

    public Boss(int x, int y) {
        this.x = x;
        this.y = y;
        this.bossProjectiles = new ArrayList<>();
    }

    public void update() {
        // Se o Boss estiver em uma investida, ele não faz outros movimentos ou ataques
        if (isCharging) {
            handleChargeAttack();
            // Apenas atualiza projéteis já existentes (se houver algum)
            for (Bullet bullet : bossProjectiles) {
                bullet.update();
            }
            bossProjectiles.removeIf(bullet -> !bullet.isVisible());
            return; // Sai do método update para não processar outras lógicas de movimento/ataque
        }

        if (x < 0) x = 0;
        if (x + BOSS_WIDTH > GamePanel.WIDTH) x = GamePanel.WIDTH - BOSS_WIDTH;

        if (!isOnGround) {
            verticalVelocity += gravity;
            y += verticalVelocity;

            if (isJumping && !hasJumpedAndNotFiredYet && System.nanoTime() - lastJumpAttackTime > JUMP_ATTACK_DELAY_NANOS) {
                fireSingleJumpProjectile();
                hasJumpedAndNotFiredYet = true;
            }
        }

        if (y + BOSS_HEIGHT >= GamePanel.GROUND_Y) {
            y = GamePanel.GROUND_Y - BOSS_HEIGHT;
            isOnGround = true;
            verticalVelocity = 0;

            if (isJumping) {
                // Ao invés de sempre 3, agora pode iniciar o bullet hell
                if (random.nextInt(100) < 20) { // Chance de 20% de bullet hell ao pousar (para testes)
                    isPerformingBulletHell = true;
                    bulletHellProjectilesFired = 0;
                    lastLandAttackTime = System.nanoTime(); 
                } else {
                    projectilesToFireOnLand = 3; // Mantém o ataque de 3 balas normal
                    lastLandAttackTime = System.nanoTime();
                }
            }
            isJumping = false;
            hasJumpedAndNotFiredYet = false;
        } else {
            isOnGround = false;
        }

        // Impede salto duplo
        if (!isJumping && isOnGround && random.nextInt(1000) < 5) {
            startJumpAttack();
            lastJumpAttackTime = System.nanoTime();
        }

        // Lógica para o ataque de pouso normal (3 projéteis)
        if (!isPerformingBulletHell && projectilesToFireOnLand > 0 && System.nanoTime() - lastLandAttackTime > LAND_ATTACK_INTERVAL_NANOS) {
            fireLandProjectile();
            projectilesToFireOnLand--;
            lastLandAttackTime = System.nanoTime();
        }

        // Lógica para a Chuva de Projéteis Acelerada (Bullet Hell)
        if (isPerformingBulletHell && bulletHellProjectilesFired < MAX_BULLET_HELL_PROJECTILES && System.nanoTime() - lastLandAttackTime > BULLET_HELL_FIRE_INTERVAL_NANOS) {
            fireBulletHellProjectile(bulletHellProjectilesFired);
            bulletHellProjectilesFired++;
            lastLandAttackTime = System.nanoTime(); 
            
            if (bulletHellProjectilesFired >= MAX_BULLET_HELL_PROJECTILES) {
                isPerformingBulletHell = false; 
            }
        }

        // Lógica para o ataque especial (chance aumentada para testes)
        if (isOnGround && System.nanoTime() - lastSpecialAttackTime > SPECIAL_ATTACK_COOLDOWN_NANOS && random.nextInt(100) < 10) { 
            fireSpecialAttack();
            lastSpecialAttackTime = System.nanoTime(); 
        }

        // NOVA LÓGICA: Ativação do ataque de Investida com Perseguição
        // Condições: No chão, cooldown passou e chance aleatória
        if (isOnGround && !isJumping && !isPerformingBulletHell && 
            System.nanoTime() - lastChargeAttackTime > CHARGE_ATTACK_COOLDOWN_NANOS && 
            random.nextInt(2000) < 10) { // Sua condição de 10 em 2000
            
            startChargeAttack();
            lastChargeAttackTime = System.nanoTime(); // Reseta o cooldown
        }

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

        for (Bullet bullet : bossProjectiles) {
            bullet.draw(g);
        }
    }

    private void startJumpAttack() {
        if (isOnGround) {
            verticalVelocity = jumpSpeed;
            isOnGround = false;
            isJumping = true;
        }
    }

    private void fireSingleJumpProjectile() {
        Player targetPlayer = getTargetPlayer();
        if (targetPlayer != null) {
            fireHomingProjectile(targetPlayer, 8, 10, 0); 
        }
    }

    private void fireLandProjectile() {
        Player targetPlayer = getTargetPlayer();
        if (targetPlayer != null) {
            fireHomingProjectile(targetPlayer, 10, 15, 0); 
        }
    }

    private void fireSpecialAttack() {
        Player targetPlayer = getTargetPlayer();
        if (targetPlayer != null) {
            fireHomingProjectile(targetPlayer, 9, 12, 5); 
            fireHomingProjectile(targetPlayer, 9, 12, -5); 
            fireHomingProjectile(targetPlayer, 9, 12, 5); 
        }
    }

    private void fireBulletHellProjectile(int projectileIndex) {
        Player targetPlayer = getTargetPlayer();
        if (targetPlayer == null) return;

        int startX = this.x + BOSS_WIDTH / 2;
        int startY = this.y + BOSS_HEIGHT / 2;

        double baseAngle = Math.atan2(targetPlayer.getY() + targetPlayer.getCurrentHeight() / 2 - startY, 
                                      targetPlayer.getX() + 40 - startX);

        double angleOffset = 0;
        int speed = 0;
        int damage = 0;

        switch (projectileIndex) {
            case 0: 
                angleOffset = Math.toRadians(-15); 
                speed = 7;
                damage = 8;
                break;
            case 1:
                angleOffset = Math.toRadians(0); 
                speed = 9;
                damage = 9;
                break;
            case 2:
                angleOffset = Math.toRadians(15);
                speed = 7;
                damage = 8;
                break;
            case 3: 
                angleOffset = Math.toRadians(-25);
                speed = 11;
                damage = 10;
                break;
            case 4:
                angleOffset = Math.toRadians(25);
                speed = 11;
                damage = 10;
                break;
            case 5: 
                angleOffset = Math.toRadians(-35);
                speed = 13;
                damage = 12;
                break;
            case 6: 
                angleOffset = Math.toRadians(35);
                speed = 13;
                damage = 12;
                break;
        }
        
        if (random.nextBoolean()) {
            double homingFactor = 0.5; 
            double angleToPlayer = Math.atan2(targetPlayer.getY() + targetPlayer.getCurrentHeight() / 2 - startY, 
                                             targetPlayer.getX() + 40 - startX);
            baseAngle = baseAngle * (1 - homingFactor) + angleToPlayer * homingFactor;
        }

        double finalAngle = baseAngle + angleOffset;

        int dx = (int) (Math.cos(finalAngle) * speed);
        int dy = (int) (Math.sin(finalAngle) * speed);

        bossProjectiles.add(new Bullet(startX, startY, 20, 20, new Point(dx, dy), damage));
    }


    private Player getTargetPlayer() {
        Player p1 = GamePanel.getInstance().getPlayer1();
        Player p2 = GamePanel.getInstance().getPlayer2();

        if (p1 != null && p2 != null) {
            return p1; 
        } else if (p1 != null) {
            return p1;
        } else if (p2 != null) {
            return p2;
        }
        return null;
    }

    private void fireHomingProjectile(Player targetPlayer, int speed, int damage, double angleVariationDegrees) {
        int targetX = targetPlayer.getX() + 40;
        int targetY = targetPlayer.getY() + targetPlayer.getCurrentHeight() / 2;

        int startX = this.x + BOSS_WIDTH / 2;
        int startY = this.y + BOSS_HEIGHT / 2;

        double angle = Math.atan2(targetY - startY, targetX - startX);

        angle += Math.toRadians(angleVariationDegrees);

        int dx = (int) (Math.cos(angle) * speed);
        int dy = (int) (Math.sin(angle) * speed);

        bossProjectiles.add(new Bullet(startX, startY, 20, 20, new Point(dx, dy), damage));
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

    public void checkBossProjectiles(Player player) {
        bossProjectiles.removeIf(bullet -> {
            if (bullet.getBounds().intersects(player.getBounds())) {
                if (player.isOnGround()) {
                    player.takeDamage(bullet.getDamage());
                }
                return true;
            }
            return false;
        });
    }

    // NOVOS MÉTODOS PARA O ATAQUE DE INVESTIDA
    private void startChargeAttack() {
        isCharging = true;
        // Decide a direção inicial da investida com base na posição do jogador
        Player targetPlayer = getTargetPlayer();
        if (targetPlayer != null) {
            if (targetPlayer.getX() < this.x) {
                chargeDirection = -1; // Jogador está à esquerda, vai para a esquerda
            } else {
                chargeDirection = 1; // Jogador está à direita, vai para a direita
            }
        } else {
            // Se não houver jogador, escolhe uma direção aleatória
            chargeDirection = random.nextBoolean() ? 1 : -1;
        }
    }

    private void handleChargeAttack() {
        x += chargeDirection * chargeSpeed;

        // Checa se atingiu o limite esquerdo ou direito da tela
        if (chargeDirection == -1 && x <= 0 + CHARGE_BUFFER_X) {
            chargeDirection = 1; // Mude para direita
        } else if (chargeDirection == 1 && x + BOSS_WIDTH >= GamePanel.WIDTH - CHARGE_BUFFER_X) {
            // Atingiu o limite direito, termina a investida
            isCharging = false;
            chargeDirection = 0; // Reseta a direção
        }
    }
}