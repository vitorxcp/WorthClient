package com.vitorxp.WorthClient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class GuiTextureLoader {

    public static ResourceLocation load(String path) {
        try {
            InputStream is = GuiTextureLoader.class.getResourceAsStream("/assets/worthclient/" + path);
            if (is == null) {
                is = GuiTextureLoader.class.getResourceAsStream(path);
            }

            if (is != null) {
                BufferedImage bi = ImageIO.read(is);
                DynamicTexture texture = new DynamicTexture(bi);
                return Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("worth_" + path, texture);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}