package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Boss {
    private int x, y;
    private int health = 2200;

    public static final int BOSS_WIDTH = 180;
    public static final int BOSS_HEIGHT = 320;

    private double verticalVelocity = 0;
    private double gravity = 3.5;
    private int jumpSpeed = -30;

    private boolean isJumping = false;
    private boolean isOnGround = true;

    private List<Bullet> bossProjectiles;

    private boolean hasJumpedAndNotFiredYet = false;
    private long lastJumpAttackTime = 0;
    private final long JUMP_ATTACK_DELAY_NANOS = 100_000_000L;

    private int projectilesToFireOnLand = 0;
    private long lastLandAttackTime = 0;
    private final long LAND_ATTACK_INTERVAL_NANOS = 150_000_000L;

    private long lastSpecialAttackTime = 0;
    private final long SPECIAL_ATTACK_COOLDOWN_NANOS = 1_500_000_000L;

    private boolean isPerformingBulletHell = false;
    private int bulletHellProjectilesFired = 0;
    private final int MAX_BULLET_HELL_PROJECTILES = 7;
    private final long BULLET_HELL_FIRE_INTERVAL_NANOS = 50_000_000L;

    private boolean isCharging = false;
    private int chargeDirection = 0;
    private int chargeSpeed = 15;
    private long lastChargeAttackTime = 0;
    private final long CHARGE_ATTACK_COOLDOWN_NANOS = 2_000_000_000L;
    private final int CHARGE_BUFFER_X = 50;

    private Random random = new Random();

    private BufferedImage bossSprite;

    // ✅ Novo controle de alternância de alvo
    private int currentTargetPlayer = 1;

    public Boss(int x, int y) {
        this.x = x;
        this.y = y;
        this.bossProjectiles = new ArrayList<>();
        this.bossSprite = SpriteManager.getSprite("boss.png");
    }

    public void update() {
        if (isCharging) {
            handleChargeAttack();
            for (Bullet bullet : bossProjectiles) {
                bullet.update();
            }
            bossProjectiles.removeIf(bullet -> !bullet.isVisible());
            return;
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
                if (random.nextInt(100) < 20) {
                    isPerformingBulletHell = true;
                    bulletHellProjectilesFired = 0;
                    lastLandAttackTime = System.nanoTime();
                } else {
                    projectilesToFireOnLand = 3;
                    lastLandAttackTime = System.nanoTime();
                }
            }
            isJumping = false;
            hasJumpedAndNotFiredYet = false;
        } else {
            isOnGround = false;
        }

        if (!isJumping && isOnGround && random.nextInt(1000) < 5) {
            startJumpAttack();
            lastJumpAttackTime = System.nanoTime();
        }

        if (!isPerformingBulletHell && projectilesToFireOnLand > 0 && System.nanoTime() - lastLandAttackTime > LAND_ATTACK_INTERVAL_NANOS) {
            fireLandProjectile();
            projectilesToFireOnLand--;
            lastLandAttackTime = System.nanoTime();
        }

        if (isPerformingBulletHell && bulletHellProjectilesFired < MAX_BULLET_HELL_PROJECTILES && System.nanoTime() - lastLandAttackTime > BULLET_HELL_FIRE_INTERVAL_NANOS) {
            fireBulletHellProjectile(bulletHellProjectilesFired);
            bulletHellProjectilesFired++;
            lastLandAttackTime = System.nanoTime();

            if (bulletHellProjectilesFired >= MAX_BULLET_HELL_PROJECTILES) {
                isPerformingBulletHell = false;
            }
        }

        if (isOnGround && System.nanoTime() - lastSpecialAttackTime > SPECIAL_ATTACK_COOLDOWN_NANOS && random.nextInt(100) < 10) {
            fireSpecialAttack();
            lastSpecialAttackTime = System.nanoTime();
        }

        if (isOnGround && !isJumping && !isPerformingBulletHell &&
            System.nanoTime() - lastChargeAttackTime > CHARGE_ATTACK_COOLDOWN_NANOS &&
            random.nextInt(2000) < 10) {

            startChargeAttack();
            lastChargeAttackTime = System.nanoTime();
        }

        for (Bullet bullet : bossProjectiles) {
            bullet.update();
        }
        bossProjectiles.removeIf(bullet -> !bullet.isVisible());
    }

    public void draw(Graphics g) {
        if (bossSprite != null) {
            g.drawImage(bossSprite, x, y, BOSS_WIDTH, BOSS_HEIGHT, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y, BOSS_WIDTH, BOSS_HEIGHT);
        }

        g.setColor(Color.WHITE);
        g.drawString("Boss HP: " + health, x, y - 10);

        for (Bullet bullet : bossProjectiles) {
            bullet.draw(g);
        }
    }

    public int getHealth() {
        return health;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, BOSS_WIDTH, BOSS_HEIGHT);
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            System.out.println("Boss derrotado!");
        }
    }

    public void checkBossProjectiles(Player player) {
        if (player == null || !player.isAlive()) return;

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

    private void startJumpAttack() {
        if (isOnGround) {
            verticalVelocity = jumpSpeed;
            isOnGround = false;
            isJumping = true;
        }
    }

    private void startChargeAttack() {
        isCharging = true;
        Player targetPlayer = getTargetPlayer();
        if (targetPlayer != null) {
            chargeDirection = (targetPlayer.getX() < this.x) ? -1 : 1;
        } else {
            chargeDirection = random.nextBoolean() ? 1 : -1;
        }
    }

    private void handleChargeAttack() {
        x += chargeDirection * chargeSpeed;

        if (chargeDirection == -1 && x <= 0 + CHARGE_BUFFER_X) {
            chargeDirection = 1;
        } else if (chargeDirection == 1 && x + BOSS_WIDTH >= GamePanel.WIDTH - CHARGE_BUFFER_X) {
            isCharging = false;
            chargeDirection = 0;
        }
    }

    // 🔁 Alterna entre os jogadores
    private Player getTargetPlayer() {
        Player p1 = GamePanel.getInstance().getPlayer1();
        Player p2 = GamePanel.getInstance().getPlayer2();

        Player target = null;
        if (currentTargetPlayer == 1 && p1 != null && p1.isAlive()) {
            target = p1;
        } else if (currentTargetPlayer == 2 && p2 != null && p2.isAlive()) {
            target = p2;
        }

        // Alterna para o próximo alvo na próxima vez
        currentTargetPlayer = (currentTargetPlayer == 1) ? 2 : 1;

        // Fallback se o jogador atual estiver morto
        if (target == null) {
            return (p1 != null && p1.isAlive()) ? p1 : p2;
        }

        return target;
    }

    private void fireHomingProjectile(Player targetPlayer, int speed, int damage, double angleVariationDegrees) {
        int targetX = targetPlayer.getX() + 40;
        int targetY = targetPlayer.getY() + targetPlayer.getCurrentHeight() / 2;

        int offsetX = -50;
        int offsetY = -90;
        int startX = this.x + BOSS_WIDTH / 2 + offsetX;
        int startY = this.y + BOSS_HEIGHT / 2 + offsetY;

        double angle = Math.atan2(targetY - startY, targetX - startX);
        angle += Math.toRadians(angleVariationDegrees);

        int dx = (int) (Math.cos(angle) * speed);
        int dy = (int) (Math.sin(angle) * speed);

        bossProjectiles.add(new Bullet(startX, startY, 20, 20, new Point(dx, dy), damage));
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

        int offsetX = -50;
        int offsetY = -90;

        int startX = this.x + BOSS_WIDTH / 2 + offsetX;
        int startY = this.y + BOSS_HEIGHT / 2 + offsetY;

        double baseAngle = Math.atan2(targetPlayer.getY() + targetPlayer.getCurrentHeight() / 2 - startY,
                                      targetPlayer.getX() + 40 - startX);

        double angleOffset = 0;
        int speed = 0;
        int damage = 0;

        switch (projectileIndex) {
            case 0: angleOffset = Math.toRadians(-15); speed = 7; damage = 8; break;
            case 1: angleOffset = Math.toRadians(0);   speed = 9; damage = 9; break;
            case 2: angleOffset = Math.toRadians(15);  speed = 7; damage = 8; break;
            case 3: angleOffset = Math.toRadians(-25); speed = 11; damage = 10; break;
            case 4: angleOffset = Math.toRadians(25);  speed = 11; damage = 10; break;
            case 5: angleOffset = Math.toRadians(-35); speed = 13; damage = 12; break;
            case 6: angleOffset = Math.toRadians(35);  speed = 13; damage = 12; break;
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
}
