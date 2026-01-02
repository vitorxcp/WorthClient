package com.vitorxp.WorthClient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CapeLoader {

    private static final Map<String, ResourceLocation> CAPE_CACHE = new HashMap<>();
    private static final Map<String, AnimatedCape> ANIMATED_CACHE = new HashMap<>();

    public static ResourceLocation loadCape(String cosmeticId) {
        if (CAPE_CACHE.containsKey(cosmeticId)) {
            return CAPE_CACHE.get(cosmeticId);
        }

        String path = "/assets/worthclient/cosmetics/capes/" + cosmeticId + ".png";

        try {
            InputStream stream = CapeLoader.class.getResourceAsStream(path);
            if (stream == null) return null;
            BufferedImage originalImage = ImageIO.read(stream);
            BufferedImage finalImage;
            int w = originalImage.getWidth();
            int h = originalImage.getHeight();
            if (w == 176 && h > 144) {
                return null;
            }
            if (w == 176) {
                System.out.println("[WorthClient] Convertendo Capa HD Estática: " + cosmeticId);
                finalImage = processHDStatic(originalImage);
            } else if (w == 92 || w > 64) {
                finalImage = parseCape(originalImage);
            } else {
                finalImage = originalImage;
            }
            DynamicTexture dynamicTexture = new DynamicTexture(finalImage);
            ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("worth_" + cosmeticId, dynamicTexture);
            CAPE_CACHE.put(cosmeticId, location);
            return location;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AnimatedCape getAnimatedCape(String cosmeticId) {
        if (ANIMATED_CACHE.containsKey(cosmeticId)) {
            return ANIMATED_CACHE.get(cosmeticId);
        }

        String path = "/assets/worthclient/cosmetics/capes/" + cosmeticId + ".png";

        try {
            InputStream stream = CapeLoader.class.getResourceAsStream(path);
            if (stream == null) return null;

            BufferedImage originalImage = ImageIO.read(stream);

            int w = originalImage.getWidth();
            int h = originalImage.getHeight();

            if (w == 176 && h > 144) {
                int frames = h / 144;
                int delay = 85;
                System.out.println("[WorthClient] Carregando animação '" + cosmeticId + "' com " + frames + " frames.");
                BufferedImage fixedImage = processAnimatedStrip(originalImage, frames);
                DynamicTexture dynTexture = new DynamicTexture(fixedImage);
                ResourceLocation dynamicLoc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("worth_anim_" + cosmeticId, dynTexture);

                AnimatedCape anime = new AnimatedCape(dynamicLoc, frames, delay);
                ANIMATED_CACHE.put(cosmeticId, anime);
                return anime;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static BufferedImage processHDStatic(BufferedImage source) {
        int destFrameWidth = 512;
        int destFrameHeight = 256;
        int scale = 8;
        int paddingY = 1 * scale;
        int srcBodyWidth = 88;
        BufferedImage finalImg = new BufferedImage(destFrameWidth, destFrameHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = finalImg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int srcYBase = 0;
        int destYBase = 0;
        int destY = destYBase + paddingY;
        int srcLeftX = 4;
        int srcRightX = srcBodyWidth + 4;
        int destFrontX = 1 * scale;
        int destBackX = 12 * scale;
        copyArea(g, source, srcLeftX, srcYBase + 8, 80, 128, destFrontX, destY);
        copyArea(g, source, srcRightX, srcYBase + 8, 80, 128, destBackX, destY);
        copyArea(g, source, srcLeftX, srcYBase, 80, 8, destFrontX, destYBase);
        copyArea(g, source, srcRightX, srcYBase, 80, 8, destBackX - 8, destYBase);
        copyArea(g, source, 0, srcYBase + 8, 8, 128, destFrontX - 8, destY);
        copyArea(g, source, 172, srcYBase + 8, 8, 128, destBackX + 80 - 4, destY);
        copyArea(g, source, 84, srcYBase + 8, 8, 128, destBackX - 8, destY);

        g.dispose();
        return finalImg;
    }
    private static BufferedImage processAnimatedStrip(BufferedImage source, int totalFrames) {
        int srcFrameHeight = 144;
        int srcBodyWidth = 88;
        int destFrameWidth = 512;
        int destFrameHeight = 256;
        int destTotalHeight = destFrameHeight * totalFrames;
        int scale = 8;
        int paddingY = 1 * scale;

        BufferedImage finalStrip = new BufferedImage(destFrameWidth, destTotalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = finalStrip.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        for (int i = 0; i < totalFrames; i++) {
            int srcYBase = i * srcFrameHeight;
            int destYBase = i * destFrameHeight;
            int srcLeftX = 4;
            int srcRightX = srcBodyWidth + 4;
            int destFrontX = 1 * scale;
            int destBackX = 12 * scale;
            int destY = destYBase + paddingY;

            copyArea(g, source, srcLeftX, srcYBase + 8, 80, 128, destFrontX, destY);
            copyArea(g, source, srcRightX, srcYBase + 8, 80, 128, destBackX, destY);
            copyArea(g, source, srcLeftX, srcYBase, 80, 8, destFrontX, destYBase);
            copyArea(g, source, srcRightX, srcYBase, 80, 8, destBackX - 8, destYBase);
            copyArea(g, source, 0, srcYBase + 8, 8, 128, destFrontX - 8, destY);
            copyArea(g, source, 172, srcYBase + 8, 8, 128, destBackX + 80 - 4, destY);
            copyArea(g, source, 84, srcYBase + 8, 8, 128, destBackX - 8, destY);
        }

        g.dispose();
        return finalStrip;
    }

    private static BufferedImage parseCape(BufferedImage img) {
        int imageWidth = 64;
        int imageHeight = 32;
        int srcWidth = img.getWidth();
        int srcHeight = img.getHeight();
        if (srcWidth == 64 && srcHeight == 32) return img;
        BufferedImage newImg = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (srcWidth == 92 && srcHeight == 44) {
            int shiftX = 8;
            g.drawImage(img, 12, 1, 22, 17, 0, 0, 44, 44, null);
            int startFrontX = 46 - shiftX;
            g.drawImage(img, 0, 1, 10, 17, startFrontX, 0, startFrontX + 44, 44, null);
            g.drawImage(img, 1, 0, 11, 1, 0, 0, 48, 1, null);
        } else {
            g.drawImage(img, 0, 0, 64, 32, null);
        }
        g.dispose();
        return newImg;
    }

    private static void copyArea(Graphics2D g, BufferedImage src, int sx, int sy, int w, int h, int dx, int dy) {
        if (sx + w > src.getWidth() || sy + h > src.getHeight()) return;
        BufferedImage part = src.getSubimage(sx, sy, w, h);
        g.drawImage(part, dx, dy, w, h, null);
    }
}