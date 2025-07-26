package com.vitorxp.SkyBlockModVX;

import com.vitorxp.SkyBlockModVX.anticheat.SuspiciousBehaviorDetector;
import com.vitorxp.SkyBlockModVX.chat.*;
import com.vitorxp.SkyBlockModVX.commands.*;
import com.vitorxp.SkyBlockModVX.events.AnnounceMutanteEvent;
import com.vitorxp.SkyBlockModVX.events.GuiMenuEvent;
import com.vitorxp.SkyBlockModVX.hud.*;
import com.vitorxp.SkyBlockModVX.keybinds.Keybinds;
import com.vitorxp.SkyBlockModVX.logger.InventoryLossLogger;
import com.vitorxp.SkyBlockModVX.manager.ActivationManager;
import com.vitorxp.SkyBlockModVX.manager.ConfigManager;
import com.vitorxp.SkyBlockModVX.manager.InventoryFullNotifier;
import com.vitorxp.SkyBlockModVX.manager.LagManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = "skyblockmodvx", name = "SkyBlockModVX", version = "1.0.1-alpha")
public class SkyBlockMod {
    public static boolean blockPetMessages = true;
    public static boolean blockInventoryMessages = true;
    public static boolean announceZealot = true;
    public static boolean MsgBlockDestroyBlock = true;
    public static boolean petOverlay = true;
    public static int zealotMessageTicksLeft = 0;
    public static boolean pingOverlay = true;
    public static boolean fpsOverlay = true;
    public static boolean mainHandHUDOverlay = true;
    public static boolean helmetHUDOverlay = true;
    public static boolean chestplateHUDOverlay = true;
    public static boolean leggingsHUDOverlay = true;
    public static boolean bootsHUDOverlay = true;
    public static boolean petDisplayViewOff = false;
    public static boolean viewsPetAll = false;
    public static boolean showTime = false;
    public static boolean enableCopy = false;
    public static boolean keystrokesOverlay = false;

    public static boolean guiEditorArmor = false;
    public static boolean guiEditorPet = false;
    public static boolean guiEditorChat = false;
    public static boolean GuiKeyEditor = false;
    public static boolean GuiOverlay= false;
    public static boolean pendingOpenMenu = false;
    public static boolean pendingOpenMenuHud = false;

    public static String currentPetName = "Desconhecido";
    public static String currentPetlevel = "0";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("SkyBlockModVX ativando...");

        ActivationManager.init();
        ConfigManager.load();
        HudPositionManager.load();
        Keybinds.init();

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PingHUD());
        MinecraftForge.EVENT_BUS.register(new AnnounceMutante());
        MinecraftForge.EVENT_BUS.register(new AnnounceMutanteEvent());
        MinecraftForge.EVENT_BUS.register(new DestroyBlock());
        MinecraftForge.EVENT_BUS.register(new InventoryFullBlock());
        MinecraftForge.EVENT_BUS.register(new PetMaxBlockChat());
        MinecraftForge.EVENT_BUS.register(new GuiMenuEvent());
        MinecraftForge.EVENT_BUS.register(new PetHud());
        MinecraftForge.EVENT_BUS.register(new PingHUD());
        MinecraftForge.EVENT_BUS.register(new FPSHUD());
        MinecraftForge.EVENT_BUS.register(new SellMessageCombiner());
        MinecraftForge.EVENT_BUS.register(new InventoryFullNotifier());
        MinecraftForge.EVENT_BUS.register(new ArmorStatusHUD());
        MinecraftForge.EVENT_BUS.register(new InventoryLossLogger());
        MinecraftForge.EVENT_BUS.register(new ChatCommandTracker());
        MinecraftForge.EVENT_BUS.register(new SuspiciousBehaviorDetector());
        MinecraftForge.EVENT_BUS.register(new ChatModifier());
        MinecraftForge.EVENT_BUS.register(new KeystrokesHUD());
        MinecraftForge.EVENT_BUS.register(new Keybinds());

        ClientCommandHandler.instance.registerCommand(new CommandPetMaxBlock());
        ClientCommandHandler.instance.registerCommand(new CommandInventoryBlock());
        ClientCommandHandler.instance.registerCommand(new CommandMutanteAnnounce());
        ClientCommandHandler.instance.registerCommand(new CommandOpenMenu());
        ClientCommandHandler.instance.registerCommand(new CommandActivate());
        ClientCommandHandler.instance.registerCommand(new CommandEditHud());
        ClientCommandHandler.instance.registerCommand(new CommandTest());
        ClientCommandHandler.instance.registerCommand(new CopyMessageCommand());

        //MinecraftForge.EVENT_BUS.register(new GlowItemRenderer()); - beta

        System.out.println("SkyBlockModVX carregado com sucesso!");

        System.out.println("SkyBlockModVX - aplicando configurações para melhora de fps...");

        GameSettings settings = Minecraft.getMinecraft().gameSettings;

        settings.fboEnable = true;
        settings.useVbo = true;
        settings.ambientOcclusion = 0;
        settings.clouds = 0;

        try {
            settings.mipmapLevels = 4;
        } catch (Exception ignored) {}

        Minecraft.getMinecraft().renderGlobal.loadRenderers();

        System.out.println("SkyBlockModVX - configurações visuais aplicadas!");

        //MinecraftForge.EVENT_BUS.register(new MenuInterceptor()); - beta

    }

    @Mod.EventHandler
    public void onShutdown(FMLServerStoppedEvent event) {
        InventoryLossLogger.saveToFile();
        HudPositionManager.save();
        ConfigManager.save();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        LagManager.onTick(event);
    }
}