package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Insets; // Importar Insets

public class Game {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame window = new JFrame();
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setResizable(false);
                window.setTitle("Cuphead Clone");

                GamePanel gamePanel = new GamePanel();
                window.add(gamePanel);

                // --- ALTERAÇÕES AQUI ---
                // 1. Chame pack() ou setVisible(true) ANTES de obter os insets
                //    para que a janela seja criada e os insets sejam calculados.
                window.pack(); 
                // OU window.setVisible(true); // Se preferir manter o pack() antes, o setVisible(true) pode vir aqui

                // 2. Obtenha os insets (bordas da janela)
                Insets insets = window.getInsets();

                // 3. Calcule o novo tamanho da janela para que a área interna (content pane)
                //    tenha exatamente WIDTH e HEIGHT.
                int windowWidth = GamePanel.WIDTH + insets.left + insets.right;
                int windowHeight = GamePanel.HEIGHT + insets.top + insets.bottom;

                // 4. Defina o tamanho final da janela
                window.setSize(windowWidth, windowHeight);
                // -----------------------

                window.setLocationRelativeTo(null); // Centraliza a janela na tela
                window.setVisible(true); // Garante que a janela esteja visível, se não estiver já

                gamePanel.requestFocusInWindow();
            }
        });
    }
}