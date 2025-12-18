package com.vitorxp.WorthClient.utils;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

public class TextureOptimizer {

    public static void optimizeAndReload() {
        Minecraft mc = Minecraft.getMinecraft();

        System.gc();

        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mc.addScheduledTask(() -> {
                System.out.println("[WorthClient] Iniciando recarregamento otimizado...");

                mc.refreshResources();

                System.gc();
                System.out.println("[WorthClient] Memória limpa após carregamento.");
            });
        }, "WorthTextureLoader").start();
    }
}