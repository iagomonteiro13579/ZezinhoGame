package main;

import java.awt.event.*; // Para KeyEvent e KeyListener
import java.util.HashSet; // Para armazenar as teclas pressionadas de forma eficiente

public class KeyHandler implements KeyListener {
    // Um HashSet é usado para armazenar os códigos das teclas que estão atualmente pressionadas.
    // Isso permite verificações rápidas se uma tecla está pressionada.
    private HashSet<Integer> keys = new HashSet<>();

    // Retorna verdadeiro se a tecla com o código 'key' estiver atualmente pressionada
    public boolean isKeyPressed(int key) {
        return keys.contains(key);
    }

    @Override
    // Chamado quando uma tecla é pressionada
    public void keyPressed(KeyEvent e) {
        keys.add(e.getKeyCode()); // Adiciona o código da tecla ao conjunto de teclas pressionadas
    }

    @Override
    // Chamado quando uma tecla é liberada
    public void keyReleased(KeyEvent e) {
        keys.remove(e.getKeyCode()); // Remove o código da tecla do conjunto de teclas pressionadas
    }

    @Override
    // Chamado quando uma tecla é digitada (pressionada e liberada, resultando em um caractere Unicode)
    // Geralmente não é usado para movimentação em jogos, por isso está vazio
    public void keyTyped(KeyEvent e) {}
}