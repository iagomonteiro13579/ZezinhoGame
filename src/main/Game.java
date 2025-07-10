package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities; // Importar SwingUtilities para garantir a execução na EDT

public class Game {

    public static void main(String[] args) {
        // Garantir que a criação e manipulação da GUI (Swing) seja feita na Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame window = new JFrame(); // Cria a janela principal
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Define o comportamento ao fechar a janela
                window.setResizable(false); // Impede que o usuário redimensione a janela
                window.setTitle("Cuphead Clone"); // Define o título da janela

                GamePanel gamePanel = new GamePanel(); // Cria uma instância do seu painel de jogo
                window.add(gamePanel); // Adiciona o painel de jogo à janela

                window.pack(); // Ajusta o tamanho da janela para se adequar ao tamanho preferencial do GamePanel
                window.setLocationRelativeTo(null); // Centraliza a janela na tela
                window.setVisible(true); // Torna a janela visível

                // Solicita o foco para o GamePanel após a janela se tornar visível
                // Isso é crucial para que o KeyListener funcione imediatamente
                gamePanel.requestFocusInWindow();
            }
        });
    }
}