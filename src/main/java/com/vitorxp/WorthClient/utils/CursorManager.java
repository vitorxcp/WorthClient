package com.vitorxp.WorthClient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.IntBuffer;

public class CursorManager {

    public static void loadCustomCursor() {
        try {
            ResourceLocation cursorLoc = new ResourceLocation("worthclient", "textures/icons/cursor.png");

            InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(cursorLoc).getInputStream();
            BufferedImage img = ImageIO.read(stream);
            stream.close();

            int width = img.getWidth();
            int height = img.getHeight();

            int[] rgb = img.getRGB(0, 0, width, height, null, 0, width);

            IntBuffer buffer = BufferUtils.createIntBuffer(width * height);

            for (int y = height - 1; y >= 0; y--) {
                for (int x = 0; x < width; x++) {
                    int pixel = rgb[y * width + x];
                    buffer.put(pixel);
                }
            }
            buffer.flip();

            int xHotspot = 10;
            int yHotspot = height - 10;

            Cursor cursor = new Cursor(width, height, xHotspot, yHotspot, 1, buffer, null);

            Mouse.setNativeCursor(cursor);

            System.out.println("[WorthClient] Cursor personalizado aplicado (Ponta alinhada)!");

        } catch (Exception e) {
            System.err.println("[WorthClient] Erro ao carregar cursor personalizado:");
            e.printStackTrace();
        }
    }
}