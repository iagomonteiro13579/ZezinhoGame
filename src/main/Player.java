package main;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int x, y;
    private int width = 80;
    private int height = 160;
    private int currentHeight;

    private int speed = 6;

    private int jumpSpeed = -60;
    private double gravity = 3.5;
    private double verticalVelocity = 0;

    private boolean isJumping = false;
    private boolean isOnGround = true;
    private boolean isDucking = false;
    private boolean fallThroughPlatform = false;

    private int groundY;

    private ArrayList<Bullet> bullets;
    private KeyHandler keyHandler;
    private int leftKey, rightKey, upKey, downKey, shootKey;

    private int health = 100;
    private boolean isAlive = true;

    private long lastShotTime = 0;
    private final long shotIntervalNanos = 100_000_000L;

    private long lastCollisionTime = 0;
    private final long collisionCooldownNanos = 1_000_000_000L; // 1 segundo

    // Novos para invulnerabilidade
    private boolean invulnerable = false;
    private long invulnerableStartTime = 0;
    private final long invulnerableDurationNanos = 1_000_000_000L; // 3 segundos

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
        if (!isAlive) return;

        // Atualiza invulnerabilidade
        if (invulnerable) {
        long now = System.nanoTime();
        if (now - invulnerableStartTime >= invulnerableDurationNanos) {
            invulnerable = false;
        }
    }

        if (keyHandler.isKeyPressed(leftKey)) x -= speed;
        if (keyHandler.isKeyPressed(rightKey)) x += speed;
        if (x < 0) x = 0;
        if (x + width > GamePanel.WIDTH) x = GamePanel.WIDTH - width;

        fallThroughPlatform = keyHandler.isKeyPressed(downKey) && keyHandler.isKeyPressed(upKey);

        if (keyHandler.isKeyPressed(upKey) && isOnGround && !fallThroughPlatform) {
            verticalVelocity = jumpSpeed;
            isOnGround = false;
        }

        if (!isOnGround) {
            verticalVelocity += gravity;
            y += verticalVelocity;
        }

        if (keyHandler.isKeyPressed(downKey) && !isDucking) {
            isDucking = true;
            y += (height - (height / 2));
            currentHeight = height / 2;
        } else if (!keyHandler.isKeyPressed(downKey) && isDucking) {
            isDucking = false;
            y -= (height - currentHeight);
            currentHeight = height;
        }

        if (y + currentHeight >= groundY) {
            y = groundY - currentHeight;
            isOnGround = true;
            verticalVelocity = 0;
        } else {
            isOnGround = false;
        }

        checkPlatformCollision(GamePanel.getInstance().getPlatforms());

        if (keyHandler.isKeyPressed(shootKey)) {
            long currentTime = System.nanoTime();
            if (currentTime - lastShotTime >= shotIntervalNanos) {
                shoot();
                lastShotTime = currentTime;
            }
        }

        for (Bullet b : bullets) b.update();
        bullets.removeIf(b -> !b.isVisible());

        Boss boss = GamePanel.getInstance().getBoss();
        if (boss != null) {
            checkBullets(boss);
        }

        checkBulletsForDamage(GamePanel.player1 == this ? GamePanel.player2.bullets : GamePanel.player1.bullets);
    }

    public void draw(Graphics g) {
        if (!isAlive) return;

        if (invulnerable) {
            // Player piscando / transparente quando invulnerável
            Graphics2D g2d = (Graphics2D) g;
            float alpha = 0.5f; // 50% transparente
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(ac);
            g2d.setColor(Color.BLUE);
            g2d.fillRect(x, y, width, currentHeight);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, width, currentHeight);
        }

        g.setColor(Color.WHITE);
        g.drawString("Player HP: " + health, x, y - 10);
        for (Bullet b : bullets) b.draw(g);
    }
    public boolean isInvulnerable() {
    return invulnerable;
}


    public void shoot() {
        bullets.add(new Bullet(x + width / 2, y + currentHeight / 2, 20, 10, 15, 1, 10));
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, currentHeight);
    }

    public void checkBullets(Boss boss) {
        bullets.removeIf(b -> {
            if (b.getBounds().intersects(boss.getBounds())) {
                boss.takeDamage(b.getDamage());
                return true;
            }
            return false;
        });
    }

    public void checkBulletsForDamage(List<Bullet> bulletsToCheck) {
        long currentTime = System.nanoTime();
        for (Bullet b : bulletsToCheck) {
            if (b.isVisible() && b.getBounds().intersects(this.getBounds())) {
                if (!invulnerable && currentTime - lastCollisionTime >= collisionCooldownNanos) {
                    takeDamage(b.getDamage());
                    invulnerable = true;
                    invulnerableStartTime = currentTime;
                    lastCollisionTime = currentTime;
                    // NÃO remover bala para atravessar
                }
            }
        }
    }

    public void checkPlatformCollision(List<Platform> platforms) {
        int playerFeetYBeforeMove = y + currentHeight - (int) verticalVelocity;
        Rectangle playerBounds = getBounds();
        boolean landedOnPlatformThisFrame = false;

        for (Platform platform : platforms) {
            Rectangle platformBounds = platform.getBounds();

            if (verticalVelocity >= 0 &&
                playerBounds.intersects(platformBounds) &&
                playerFeetYBeforeMove <= platformBounds.y &&
                x + width > platformBounds.x &&
                x < platformBounds.x + platformBounds.width &&
                !fallThroughPlatform) {

                y = platformBounds.y - currentHeight;
                verticalVelocity = 0;
                isOnGround = true;
                landedOnPlatformThisFrame = true;
                break;
            }
        }

        if (!landedOnPlatformThisFrame && y + currentHeight < groundY) {
            isOnGround = false;
        } else if (!landedOnPlatformThisFrame && y + currentHeight >= groundY) {
            y = groundY - currentHeight;
            isOnGround = true;
            verticalVelocity = 0;
        }
    }

    public void checkBossCollision(Boss boss) {
        if (getBounds().intersects(boss.getBounds())) {
            long currentTime = System.nanoTime();
            if (currentTime - lastCollisionTime >= collisionCooldownNanos) {
                takeDamage(10);
                lastCollisionTime = currentTime;
            }
        }
    }

   public void takeDamage(int damage) {
    if (!invulnerable) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            isAlive = false;
        }
        invulnerable = true;
        invulnerableStartTime = System.nanoTime();
    }
}

    public int getHealth() {
        return health;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public int getCurrentHeight() {
        return currentHeight;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public boolean isAlive() {
        return isAlive;
    }
}
