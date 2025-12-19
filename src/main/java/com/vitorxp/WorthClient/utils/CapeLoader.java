package com.vitorxp.WorthClient.utils;

import net.minecraft.util.ResourceLocation;

public class CapeLoader {
    public static ResourceLocation loadCape(String cosmeticId) {
        if (cosmeticId.equals("cape_free_redeworth")) {
                return new ResourceLocation("worthclient", "cosmetics/capes/cape_free_redeworth.png");
        }
        return null;
    }
}