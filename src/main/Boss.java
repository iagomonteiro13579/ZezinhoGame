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
    // O intervalo para o ataque de pouso original, ajustado para ser mais rápido na sequência
    private final long LAND_ATTACK_INTERVAL_NANOS = 150_000_000L; 

    // Variáveis para o novo ataque especial (mantidas como no código anterior)
    private long lastSpecialAttackTime = 0;
    private final long SPECIAL_ATTACK_COOLDOWN_NANOS = 100_000_000L; // Cooldown de 2 segundos

    // Novas variáveis para a Chuva de Projéteis Acelerada
    private boolean isPerformingBulletHell = false;
    private int bulletHellProjectilesFired = 0;
    private final int MAX_BULLET_HELL_PROJECTILES = 7; // Total de projéteis na chuva
    private final long BULLET_HELL_FIRE_INTERVAL_NANOS = 70_000_000L; // Intervalo entre cada projétil da chuva (70ms)


    private Random random = new Random();

    public Boss(int x, int y) {
        this.x = x;
        this.y = y;
        this.bossProjectiles = new ArrayList<>();
    }

    public void update() {
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
                if (random.nextInt(2000) < 5) { // Sua condição de 5 em 2000
                    isPerformingBulletHell = true;
                    bulletHellProjectilesFired = 0;
                    lastLandAttackTime = System.nanoTime(); // Reutiliza este tempo para o bullet hell
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
            lastLandAttackTime = System.nanoTime(); // Atualiza para o próximo projétil da sequência
            
            if (bulletHellProjectilesFired >= MAX_BULLET_HELL_PROJECTILES) {
                isPerformingBulletHell = false; // Termina o ataque de chuva de projéteis
            }
        }


        // Lógica para o ataque especial (mantida)
        if (isOnGround && System.nanoTime() - lastSpecialAttackTime > SPECIAL_ATTACK_COOLDOWN_NANOS && random.nextInt(1000) < 10) {
            fireSpecialAttack();
            lastSpecialAttackTime = System.nanoTime(); // Reseta o cooldown
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
            fireHomingProjectile(targetPlayer, 8, 10, 0); // Sem variação extra de ângulo para este
        }
    }

    // Método original para 3 projéteis de pouso (se não for bullet hell)
    private void fireLandProjectile() {
        Player targetPlayer = getTargetPlayer();
        if (targetPlayer != null) {
            fireHomingProjectile(targetPlayer, 10, 15, 0); // Sem variação extra de ângulo para este
        }
    }

    // Novo método para o ataque especial (mantido)
    private void fireSpecialAttack() {
        Player targetPlayer = getTargetPlayer();
        if (targetPlayer != null) {
            fireHomingProjectile(targetPlayer, 9, 12, 5); // Bala central com pequena variação
            fireHomingProjectile(targetPlayer, 9, 12, -5); // Bala desviada para um lado
            fireHomingProjectile(targetPlayer, 9, 12, 5); // Bala desviada para o outro lado
        }
    }

    // NOVO: Método para disparar um projétil da chuva de balas
    private void fireBulletHellProjectile(int projectileIndex) {
        Player targetPlayer = getTargetPlayer();
        if (targetPlayer == null) return;

        int startX = this.x + BOSS_WIDTH / 2;
        int startY = this.y + BOSS_HEIGHT / 2;

        // Calcular o ângulo base para o jogador
        double baseAngle = Math.atan2(targetPlayer.getY() + targetPlayer.getCurrentHeight() / 2 - startY, 
                                      targetPlayer.getX() + 40 - startX);

        // Definir a variação do ângulo e velocidade para criar o efeito de leque e aceleração
        double angleOffset = 0;
        int speed = 0;
        int damage = 0;

        // Distribuir os projéteis em um leque e variar a velocidade
        switch (projectileIndex) {
            case 0: // Projétil inicial, mais lento e mais centrado
                angleOffset = Math.toRadians(-15); // Exemplo de desvio
                speed = 7;
                damage = 8;
                break;
            case 1:
                angleOffset = Math.toRadians(0); // Centrado
                speed = 9;
                damage = 9;
                break;
            case 2:
                angleOffset = Math.toRadians(15);
                speed = 7;
                damage = 8;
                break;
            case 3: // Mais rápido
                angleOffset = Math.toRadians(-25);
                speed = 11;
                damage = 10;
                break;
            case 4:
                angleOffset = Math.toRadians(25);
                speed = 11;
                damage = 10;
                break;
            case 5: // Mais rápido ainda
                angleOffset = Math.toRadians(-35);
                speed = 13;
                damage = 12;
                break;
            case 6: // Final e mais rápido
                angleOffset = Math.toRadians(35);
                speed = 13;
                damage = 12;
                break;
        }
        
        // Pequena chance de ser teleguiado (50%)
        if (random.nextBoolean()) {
             // Redireciona um pouco mais para o jogador, adicionando um fator teleguiado
            double homingFactor = 0.5; // Ajuste este valor para mais ou menos "homing"
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
            return p1; // Preferência por p1
        } else if (p1 != null) {
            return p1;
        } else if (p2 != null) {
            return p2;
        }
        return null;
    }

    // Método fireHomingProjectile atualizado para aceitar variação de ângulo
    private void fireHomingProjectile(Player targetPlayer, int speed, int damage, double angleVariationDegrees) {
        int targetX = targetPlayer.getX() + 40;
        int targetY = targetPlayer.getY() + targetPlayer.getCurrentHeight() / 2;

        int startX = this.x + BOSS_WIDTH / 2;
        int startY = this.y + BOSS_HEIGHT / 2;

        double angle = Math.atan2(targetY - startY, targetX - startX);

        // Aplica a variação de ângulo
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
}