package com.vitorxp.WorthClient;

import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class WindowUtils {

    private static final String ICON_PATH = "/assets/worthclient/icons/icon.png";
    private static final String WINDOW_TITLE = "WorthClient";

    public static void applyWindowStyle() {
        if (Display.isCreated()) {
            try {
                setTitle(WINDOW_TITLE);
                setIcon(ICON_PATH);
                WorthClient.logger.info("Ícone e Título da janela definidos com sucesso.");
            } catch (Exception e) {
                System.err.println("[WorthClient] Erro ao definir ícone/título: " + e.getMessage());
            }
        }
    }

    public static void setTitle(String title) {
        Display.setTitle(title);
    }

    public static void setIcon(String path) throws IOException {
        InputStream stream = WindowUtils.class.getResourceAsStream(path);

        if (stream == null) {
            System.err.println("[WorthClient] Ícone não encontrado no caminho: " + path);
            return;
        }

        BufferedImage sourceImage = ImageIO.read(stream);
        List<ByteBuffer> icons = new ArrayList<>();

        icons.add(convertImageToBuffer(resizeImage(sourceImage, 16, 16)));
        icons.add(convertImageToBuffer(resizeImage(sourceImage, 32, 32)));
        icons.add(convertImageToBuffer(resizeImage(sourceImage, 64, 64)));
        icons.add(convertImageToBuffer(sourceImage)); // Adiciona original também

        Display.setIcon(icons.toArray(new ByteBuffer[0]));
    }

    private static BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private static ByteBuffer convertImageToBuffer(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * image.getWidth() * image.getHeight());

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];

                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();
        return buffer;
    }
}