package main;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.awt.AlphaComposite; // Importar para controlar a transparência

public class GamePanel extends JPanel implements Runnable {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int GROUND_Y = 540; // Altura do chão, Y=540.

    private Thread thread;
    private boolean running;

    private Player player1, player2;
    private Boss boss;
    private KeyHandler keyHandler;

    private BufferedImage backgroundImage;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setDoubleBuffered(true);

        keyHandler = new KeyHandler();
        addKeyListener(keyHandler);

        setFocusable(true);

        // --- Carregar a imagem de background ---
        try {
            URL imageUrl = getClass().getResource("/res/background/background.png");
            if (imageUrl == null) {
                System.err.println("Erro: Imagem de background não encontrada! Verifique o caminho: /res/background/background.png");
                setBackground(Color.CYAN); // Fallback para uma cor
            } else {
                backgroundImage = ImageIO.read(imageUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar a imagem de background: " + e.getMessage());
            setBackground(Color.CYAN); // Fallback para uma cor
        }
        // ---------------------------------------
        
        startGame();
    }

    public void startGame() {
        player1 = new Player(100, GROUND_Y, keyHandler, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_F, GROUND_Y);
        player2 = new Player(200, GROUND_Y, keyHandler, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER, GROUND_Y);
        
        // --- ALTERAÇÃO AQUI: Posicionar o Boss em GROUND_Y ---
        // Para posicionar o Boss no chão, precisamos saber a altura dele.
        // Assumindo a altura padrão do Boss de 50 pixels (conforme Boss.java):
        int bossHeight = 300; // Você pode obter isso da classe Boss se ela tiver uma constante, ou manter assim
        boss = new Boss(700, GROUND_Y - bossHeight); // O Boss agora será posicionado em 540 (GROUND_Y) - 50 (altura) = Y=490
        // ------------------------------------------------------

        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override // ESTE @Override É OBRIGATÓRIO AQUI!
    public void run() {
        double drawInterval = 1000000000.0 / 60;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

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

    // Este método 'update()' NÃO deve ter @Override
    public void update() {
        player1.update();
        player2.update();
        boss.update();

        player1.checkBullets(boss);
        player2.checkBullets(boss);
    }

    @Override // Este está correto, pois paintComponent() é da superclasse JPanel
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Casting para Graphics2D para usar recursos avançados como transparência
        Graphics2D g2d = (Graphics2D) g;

        // --- Desenhar a imagem de background (OPACO - alpha 1.0f) ---
        if (backgroundImage != null) {
            // Define o composite para total opacidade para a imagem de background
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null);
        } else {
            // Se a imagem não carregou, desenha a cor de fallback (ainda opaco)
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
        }

        // --- Desenhar o "chão" visual (AGORA TRANSPARENTE - alpha 0.0f) ---
        // Cria um AlphaComposite para total transparência
        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f);
        g2d.setComposite(alpha); // Aplica a transparência

        // As linhas abaixo AGORA SERÃO INVISÍVEIS por causa do AlphaComposite
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, GROUND_Y + 40, WIDTH, HEIGHT - (GROUND_Y + 40));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(0, GROUND_Y + 40, WIDTH, GROUND_Y + 40);

        // --- Resetar a transparência para os objetos do jogo (JOGADOR, CHEFE, BALAS) ---
        // É CRUCIAL resetar o AlphaComposite para 1.0f (totalmente opaco)
        // antes de desenhar os outros elementos do jogo, caso contrário, eles também ficarão transparentes!
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        player1.draw(g2d);
        player2.draw(g2d);
        boss.draw(g2d);
    }
}