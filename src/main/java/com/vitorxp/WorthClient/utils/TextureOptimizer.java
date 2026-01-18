package com.vitorxp.WorthClient.utils;

import net.minecraft.client.Minecraft;

public class TextureOptimizer {

    public static void optimizeAndReload() {
        Minecraft mc = Minecraft.getMinecraft();

        System.gc();
        System.runFinalization();

        new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mc.addScheduledTask(() -> {
                System.out.println("[WorthClient] Iniciando recarregamento otimizado...");
                long start = System.currentTimeMillis();

                mc.refreshResources();

                long end = System.currentTimeMillis();
                System.out.println("[WorthClient] Recarregamento finalizado em " + (end - start) + "ms.");

                System.gc();
            });
        }, "WorthTextureLoader").start();
    }
}