#!/bin/bash

echo "🛠️ Compilando o jogo..."
javac -d out src/main/*.java

echo "🚀 Executando o jogo..."
# Adicione a opção -Dsun.java2d.uiScale=1.0 para desabilitar o escalonamento automático de DPI pelo Java
# Isso fará com que Toolkit.getDefaultToolkit().getScreenSize() retorne a resolução nativa.
java -Dsun.java2d.uiScale=1.0 -cp out main.Game