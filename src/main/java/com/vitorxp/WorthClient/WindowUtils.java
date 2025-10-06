package com.vitorxp.WorthClient;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class WindowUtils {

    private static boolean applied = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!applied && Display.isCreated()) {
            try {
                setTitle("Rede Worth - SkyBlock");
                setIcon();
                applied = true;
            } catch (Exception ignored) {}
        }
    }

    public static void setTitle(String title) {
        Display.setTitle(title);
    }

    public static void setIcon() throws IOException {
        List<ByteBuffer> icons = new ArrayList<>();
        icons.add(loadIcon("/assets/worthclient/icons/Logo_Worth_16x16.png"));
        icons.add(loadIcon("/assets/worthclient/icons/Logo_Worth_32x32.png"));
        icons.add(loadIcon("/assets/worthclient/icons/icon.png"));
        icons.add(loadIcon("/assets/worthclient/icons/Logo_Worth_128x128.png"));

        Display.setIcon(icons.toArray(new ByteBuffer[0]));
    }

    private static ByteBuffer loadIcon(String path) throws IOException {
        BufferedImage image = ImageIO.read(WindowUtils.class.getResourceAsStream(path));
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
