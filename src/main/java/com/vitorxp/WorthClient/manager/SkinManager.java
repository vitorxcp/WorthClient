package com.vitorxp.WorthClient.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

import java.io.File;

public class SkinManager {

    public static ResourceLocation getSkin(String username) {
        if (username == null || username.isEmpty()) return new ResourceLocation("textures/entity/steve.png");

        String filename = username.toLowerCase();
        ResourceLocation skinLocation = new ResourceLocation("skins/" + filename);

        ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(skinLocation);

        if (texture == null) {
            File resourceDir = new File(Minecraft.getMinecraft().mcDataDir, "cached_skins");
            if(!resourceDir.exists()) resourceDir.mkdirs();

            File skinFile = new File(resourceDir, filename + ".png");

            ThreadDownloadImageData imageData = new ThreadDownloadImageData(skinFile, "https://minotar.net/skin/" + username, new ResourceLocation("textures/entity/steve.png"), null);
            Minecraft.getMinecraft().getTextureManager().loadTexture(skinLocation, imageData);
        }

        return skinLocation;
    }
}