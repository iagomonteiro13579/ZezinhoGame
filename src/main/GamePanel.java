package main;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {

    public static int WIDTH = 800;
    public static int HEIGHT = 600;
    public static int GROUND_Y;

    private Thread thread;
    private boolean running;
    private boolean fullscreen = false;
    private boolean gameOver = false;

    public static GameState gameState = GameState.RUNNING;

    public static Player player1;
    public static Player player2;
    public static Boss boss;

    private KeyHandler keyHandler;

    private BufferedImage backgroundImage;
    private List<Platform> platforms;

    private int selectedOption = 0;
    private String[] menuOptions = {
        "Tela: Janela",
        "Jogadores: 1",
        "Som: Ligado",
        "Voltar ao jogo"
    };

    private GraphicsDevice graphicsDevice;
    private JFrame fullScreenFrame;

    enum GameState {
        RUNNING, MENU
    }

    private static GamePanel instance;

    public GamePanel() {
        instance = this;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDoubleBuffered(true);

        keyHandler = new KeyHandler();
        addKeyListener(keyHandler);

        setFocusable(true);

        try {
            URL imageUrl = getClass().getResource("/res/background/background.png");
            if (imageUrl != null) backgroundImage = ImageIO.read(imageUrl);
            else setBackground(Color.CYAN);
        } catch (IOException e) {
            e.printStackTrace();
            setBackground(Color.CYAN);
        }

        graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        platforms = new ArrayList<>();

        startGame();
    }

    public static GamePanel getInstance() {
        return instance;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public void setupGameObjects() {
        GROUND_Y = HEIGHT - 60;

        player1 = new Player(100, GROUND_Y, keyHandler, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_F, GROUND_Y);
        player2 = new Player(200, GROUND_Y, keyHandler, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER, GROUND_Y);

        int bossHeight = Boss.BOSS_HEIGHT;
        int bossX = WIDTH - Boss.BOSS_WIDTH - 100;
        int bossY = GROUND_Y - bossHeight;
        boss = new Boss(bossX, bossY);

        platforms.clear();

        // POSIÇÕES ORIGINAIS DAS PLATAFORMAS
        int p1X = 200;
        int p1Y = 650;
        int p3X = bossX - 1150;
        int p3Y = 645;
        int p2X = 400;
        int p2Y = 250;

        int platW = 100, platH = 20;
        platforms.add(new Platform(p1X, p1Y, platW, platH));
        platforms.add(new Platform(p2X, p2Y, platW, platH));
        platforms.add(new Platform(p3X, p3Y, platW, platH));
    }

    public void startGame() {
        setupGameObjects();
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1e9 / 60, delta = 0;
        long lastTime = System.nanoTime(), currentTime;
        while (running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        if (gameState == GameState.MENU) return;
        player1.update();
        player2.update();
        boss.update();
        player1.checkBullets(boss);
        player2.checkBullets(boss);

         if (boss != null) {
        player1.checkBossCollision(boss);
        player2.checkBossCollision(boss);
    }
        // Verifica se ambos jogadores morreram
        if (!player1.isAlive() && !player2.isAlive()) {
            triggerGameOver();
        }
    }

    public void restartGame() {
        gameOver = false;
        gameState = GameState.RUNNING;
        setupGameObjects();
    }

    private void triggerGameOver() {
        gameOver = true;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (gameOver) {
            // Tela preta
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            // Texto "GAME OVER"
            g2d.setFont(new Font("Arial", Font.BOLD, 64));
            g2d.setColor(Color.WHITE);
            String text = "GAME OVER";
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            int textHeight = g2d.getFontMetrics().getAscent();
            g2d.drawString(text, (WIDTH - textWidth) / 2, (HEIGHT + textHeight) / 2);
            return;
        }

        if (backgroundImage != null)
            g2d.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null);
        else {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
        }

        AlphaComposite a = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0f);
        g2d.setComposite(a);
        g2d.fillRect(0, GROUND_Y + 40, WIDTH, HEIGHT - (GROUND_Y + 40));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(0, GROUND_Y + 40, WIDTH, GROUND_Y + 40);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        for (Platform p : platforms) p.draw(g2d);

        if (gameState == GameState.RUNNING) {
            player1.draw(g2d);
            player2.draw(g2d);
            boss.draw(g2d);
        } else {
            drawMenu(g2d);
        }
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        for (int i = 0; i < menuOptions.length; i++) {
            g.setColor(i == selectedOption ? Color.YELLOW : Color.WHITE);
            g.drawString(menuOptions[i], 100, 150 + i * 50);
        }
    }

    public static void toggleMenu() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.MENU;
        } else {
            gameState = GameState.RUNNING;
        }
    }

    public static void moveMenuSelection(int direction) {
        GamePanel gp = getInstance();
        gp.selectedOption = (gp.selectedOption + direction + gp.menuOptions.length) % gp.menuOptions.length;
    }

    public static void selectMenuOption() {
        GamePanel gp = getInstance();
        switch (gp.selectedOption) {
            case 0:
                gp.fullscreen = !gp.fullscreen;
                if (gp.fullscreen) {
                    gp.menuOptions[0] = "Tela: Fullscreen";
                    gp.applyFullscreen();
                } else {
                    gp.menuOptions[0] = "Tela: Janela";
                    gp.applyWindowed();
                }
                break;
            case 1:
                gp.menuOptions[1] = gp.menuOptions[1].contains("1") ? "Jogadores: 2" : "Jogadores: 1";
                break;
            case 2:
                gp.menuOptions[2] = gp.menuOptions[2].contains("Ligado") ? "Som: Desligado" : "Som: Ligado";
                break;
            case 3:
                gameState = GameState.RUNNING;
                break;
        }
    }

    private void applyFullscreen() {
        JFrame window = GamePanelWindowHolder.getWindow();
        if (window != null) {
            if (!graphicsDevice.isFullScreenSupported()) {
                window.dispose();
                window.setUndecorated(true);
                window.setExtendedState(JFrame.MAXIMIZED_BOTH);

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                WIDTH = screenSize.width;
                HEIGHT = screenSize.height;

                setPreferredSize(new Dimension(WIDTH, HEIGHT));
                setSize(new Dimension(WIDTH, HEIGHT));
                window.add(this);
                window.pack();
                window.setVisible(true);
                window.revalidate();
                window.repaint();
                requestFocusInWindow();
                setupGameObjects();
                return;
            }

            fullScreenFrame = window;

            fullScreenFrame.dispose();
            fullScreenFrame.setUndecorated(true);
            fullScreenFrame.setResizable(false);

            fullScreenFrame.add(this);

            graphicsDevice.setFullScreenWindow(fullScreenFrame);

            WIDTH = graphicsDevice.getDisplayMode().getWidth();
            HEIGHT = graphicsDevice.getDisplayMode().getHeight();

            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setSize(new Dimension(WIDTH, HEIGHT));

            fullScreenFrame.revalidate();
            fullScreenFrame.repaint();

            requestFocusInWindow();
            setupGameObjects();
        }
    }

    private void applyWindowed() {
        JFrame window = GamePanelWindowHolder.getWindow();
        if (window != null) {
            if (graphicsDevice.getFullScreenWindow() != null) {
                graphicsDevice.setFullScreenWindow(null);
                if (fullScreenFrame != null) {
                    fullScreenFrame.dispose();
                    fullScreenFrame.setUndecorated(false);
                    fullScreenFrame.setResizable(false);
                    fullScreenFrame.setTitle("Cuphead Clone");
                    fullScreenFrame.add(this);
                    window = fullScreenFrame;
                } else {
                    window.dispose();
                    window.setUndecorated(false);
                    window.setResizable(false);
                    window.setTitle("Cuphead Clone");
                    window.add(this);
                }
            } else {
                window.dispose();
            }

            WIDTH = 800;
            HEIGHT = 600;

            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setSize(new Dimension(WIDTH, HEIGHT));

            window.pack();

            Insets insets = window.getInsets();
            int windowWidth = WIDTH + insets.left + insets.right;
            int windowHeight = HEIGHT + insets.top + insets.bottom;
            window.setSize(windowWidth, windowHeight);

            window.setLocationRelativeTo(null);
            window.setVisible(true);

            window.revalidate();
            window.repaint();

            requestFocusInWindow();
            setupGameObjects();
        }
    }

    public static Player getPlayer1() {
        return player1;
    }

    public static Player getPlayer2() {
        return player2;
    }

    public static Boss getBoss() {
        return boss;
    }

}
