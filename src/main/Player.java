package main;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int x, y;
    private int width = 80;
    private int height = 160;
    private int currentHeight;
    

    private BufferedImage currentSprite;
    private int animationFrame = 0;
    private int frameCounter = 0;
    private final int ANIMATION_SPEED = 8;

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
    private final long collisionCooldownNanos = 1_000_000_000L;

    private boolean invulnerable = false;
    private long invulnerableStartTime = 0;
    private final long invulnerableDurationNanos = 1_000_000_000L;

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
        this.groundY = groundY;
        bullets = new ArrayList<>();
    }

    public void update() {
        if (!isAlive) return;

        // Atualiza sprite
        updateSprite();

        if (invulnerable && System.nanoTime() - invulnerableStartTime >= invulnerableDurationNanos) {
            invulnerable = false;
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

        if (keyHandler.isKeyPressed(downKey)) {
            if (!isDucking) {
                isDucking = true;
                y += (height / 2);
                currentHeight = height / 2;
            }
        } else {
            if (isDucking) {
                isDucking = false;
                y -= (height / 2);
                currentHeight = height;
            }
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

        bullets.forEach(Bullet::update);
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
            Graphics2D g2d = (Graphics2D) g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2d.drawImage(currentSprite, x, y, width, currentHeight, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            g.drawImage(currentSprite, x, y, width, currentHeight, null);
        }

        g.setColor(Color.WHITE);
        g.drawString("Player HP: " + health, x, y - 10);
        bullets.forEach(b -> b.draw(g));
    }

     private void updateSprite() {
        frameCounter++;
        if (frameCounter >= ANIMATION_SPEED) {
            frameCounter = 0;
            animationFrame = (animationFrame + 1) % 4;
        }

        if (!isAlive) return;

        if (keyHandler.isKeyPressed(downKey)) {
            currentSprite = SpriteManager.getSprite("abaixado-removebg-preview.png");
        } else if (!isOnGround) {
            int puloIndex = Math.min(animationFrame, 3);
            currentSprite = SpriteManager.getSprite("pulo_" + (puloIndex + 1) + "-removebg-preview.png");
        } else if (keyHandler.isKeyPressed(leftKey) || keyHandler.isKeyPressed(rightKey)) {
            currentSprite = SpriteManager.getSprite("andando_" + (animationFrame % 2 + 1) + "-removebg-preview.png");
        } else if (keyHandler.isKeyPressed(shootKey)) {
            currentSprite = SpriteManager.getSprite("atirando_1-removebg-preview.png");
        } else {
            currentSprite = SpriteManager.getSprite("parado_1-removebg-preview.png");
        }
    }

   public void shoot() {
    int bulletX = x + width / 2 + 10;
    int bulletY = y + currentHeight / 2 - 30; // Sobe 30 pixels
    bullets.add(new Bullet(bulletX, bulletY, 20, 10, 10, 1, 5));
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

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, currentHeight);
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public int getHealth() {
        return health;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public int getWidth() { return width; }

    public int getCurrentHeight() { return currentHeight; }
} 