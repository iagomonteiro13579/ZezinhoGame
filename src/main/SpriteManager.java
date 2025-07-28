package main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SpriteManager {

    private static final String SPRITE_PATH = "out/res/background/"; // Caminho local para os sprites
    private static final HashMap<String, BufferedImage> spriteCache = new HashMap<>();

    public static BufferedImage getSprite(String filename) {
        if (spriteCache.containsKey(filename)) {
            return spriteCache.get(filename);
        }

        try {
            File spriteFile = new File(SPRITE_PATH + filename);
            BufferedImage sprite = ImageIO.read(spriteFile);
            spriteCache.put(filename, sprite);
            return sprite;
        } catch (IOException e) {
            System.err.println("❌ Erro ao carregar sprite: " + filename + " em " + SPRITE_PATH);
            return null;
        }
    }
}
