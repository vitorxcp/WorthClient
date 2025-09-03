package com.vitorxp.SkyBlockModVX.config;

import net.minecraftforge.common.config.Configuration;


import java.io.File;


public class PerfConfig {
    private static Configuration cfg;


    public static boolean dynTuneEnabled = true;
    public static int fpsMin = 40;
    public static int particleCap = 600;
    public static int particleBurstCap = 80;
    public static int entityCullDistance = 48;
    public static boolean cullArmorStands = true;
    public static boolean cullDroppedItems = true;
    public static boolean limitSounds = true;
    public static int duplicateSoundCooldownMs = 120;
    public static int minRenderChunks = 6;
    public static int maxRenderChunks = 16;
    public static boolean lightOptEnabled = true;
    public static int lightUpdatesPerTick = 10;
    public static boolean skipVoidLight = true;
    public static boolean autoRenderDistance = true;
    public static int fpsTarget = 60;
    public static int minRenderDistance = 4;


    public static void load(File f) {
        cfg = new Configuration(f);
        cfg.load();


        dynTuneEnabled = cfg.getBoolean("dynTuneEnabled", "tuner", dynTuneEnabled, "Auto-ajuste de gráficos baseado em FPS/TPS");
        fpsTarget = cfg.getInt("fpsTarget", "tuner", fpsTarget, 30, 300, "FPS alvo para restaurar qualidade");
        fpsMin = cfg.getInt("fpsMin", "tuner", fpsMin, 10, 120, "FPS mínimo para modo agressivo");


        particleCap = cfg.getInt("particleCap", "particles", particleCap, 50, 5000, "Limite total de partículas vivas");
        particleBurstCap = cfg.getInt("particleBurstCap", "particles", particleBurstCap, 5, 1000, "Limite por tick para novas partículas");


        entityCullDistance = cfg.getInt("entityCullDistance", "render", entityCullDistance, 16, 256, "Distância de culling para entidades leves");
        cullArmorStands = cfg.getBoolean("cullArmorStands", "render", cullArmorStands, "Ignora render de armor stands distantes");
        cullDroppedItems = cfg.getBoolean("cullDroppedItems", "render", cullDroppedItems, "Ignora render de itens dropados distantes");


        limitSounds = cfg.getBoolean("limitSounds", "sound", limitSounds, "Evita spam do mesmo som repetido");
        duplicateSoundCooldownMs = cfg.getInt("duplicateSoundCooldownMs", "sound", duplicateSoundCooldownMs, 0, 1000, "Janela de supressão de som duplicado");


        autoRenderDistance = cfg.getBoolean("autoRenderDistance", "chunks", autoRenderDistance, "Ajusta renderDistanceChunks dinamicamente");
        minRenderChunks = cfg.getInt("minRenderChunks", "chunks", minRenderChunks, 2, 16, "Mínimo de chunks de render");
        maxRenderChunks = cfg.getInt("maxRenderChunks", "chunks", maxRenderChunks, 6, 32, "Máximo de chunks de render");


        if (cfg.hasChanged()) cfg.save();
    }
}