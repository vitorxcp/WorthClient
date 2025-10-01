package com.vitorxp.SkyBlockModVX;

import com.vitorxp.SkyBlockModVX.anticheat.SuspiciousBehaviorDetector;
import com.vitorxp.SkyBlockModVX.chat.*;
import com.vitorxp.SkyBlockModVX.commands.*;
import com.vitorxp.SkyBlockModVX.config.KeystrokesColors;
import com.vitorxp.SkyBlockModVX.config.PerfConfig;
import com.vitorxp.SkyBlockModVX.events.AnnounceMutanteEvent;
import com.vitorxp.SkyBlockModVX.events.GuiMenuEvent;
import com.vitorxp.SkyBlockModVX.gui.AdminGui;
import com.vitorxp.SkyBlockModVX.handlers.PlayerInspectorHandler;
import com.vitorxp.SkyBlockModVX.handlers.RadarInteractionHandler;
import com.vitorxp.SkyBlockModVX.hud.*;
import com.vitorxp.SkyBlockModVX.keybinds.Keybinds;
import com.vitorxp.SkyBlockModVX.logger.InventoryLossLogger;
import com.vitorxp.SkyBlockModVX.manager.ActivationManager;
import com.vitorxp.SkyBlockModVX.manager.ConfigManager;
import com.vitorxp.SkyBlockModVX.manager.InventoryFullNotifier;
import com.vitorxp.SkyBlockModVX.manager.LagManager;
import com.vitorxp.SkyBlockModVX.optimization.*;

import com.vitorxp.SkyBlockModVX.rpc.DiscordRPC;
import com.vitorxp.SkyBlockModVX.utils.PerspectiveMod;
import com.vitorxp.SkyBlockModVX.utils.ZoomHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod(modid = "skyblockmodvx", name = "SkyBlockModVX", version = "1.0.1-alpha", dependencies = "after:optifine", clientSideOnly = true)
public class SkyBlockMod {

    public static boolean blockPetMessages = true;
    public static boolean blockInventoryMessages = true;
    public static boolean announceZealot = true;
    public static boolean MsgBlockDestroyBlock = true;
    public static boolean petOverlay = true;
    public static int zealotMessageTicksLeft = 0;
    public static boolean pingOverlay = true;
    public static boolean fpsOverlay = true;
    public static boolean RadarOverlay = false;

    public static boolean mainHandHUDOverlay = true;
    public static boolean helmetHUDOverlay = true;
    public static boolean chestplateHUDOverlay = true;
    public static boolean leggingsHUDOverlay = true;
    public static boolean bootsHUDOverlay = true;
    public static boolean PerspectiveModToggle = false;

    public static boolean petDisplayViewOff = false;
    public static boolean viewsPetAll = false;
    public static boolean showTime = false;
    public static boolean enableCopy = false;
    public static boolean keystrokesOverlay = false;

    public static boolean guiEditorArmor = false;
    public static boolean guiEditorPet = false;
    public static boolean guiEditorChat = false;
    public static boolean GuiKeyEditor = false;
    public static boolean GuiOverlay = false;
    public static boolean GuiPerspective = false;
    public static boolean GuiAdminPainel = false;
    public static boolean GuiPainelAdminP = false;

    public static boolean pendingOpenMenu = false;
    public static boolean pendingOpenMenuHud = false;

    public static boolean perspective = false;

    public static String currentPetName = "Desconhecido";
    public static String currentPetlevel = "0";

    public static HudManager hudManager;
    public static KeystrokesManager keystrokesManager;

    public static PerspectiveMod perspectiveMod = new PerspectiveMod();
    public static boolean guiEditorAdmin = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        PerfConfig.load(e.getSuggestedConfigurationFile());
    }

    public static final Map<String, String> pendingPlayersTP = new ConcurrentHashMap<>();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("SkyBlockModVX ativando...");

        String clientId = "1325483160011804754";
        DiscordRPC.start(clientId);

        ActivationManager.init();
        ConfigManager.load();
        HudPositionManager.load();
        Keybinds.init();
        KeystrokesColors.loadColors();

        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(new com.vitorxp.SkyBlockModVX.WindowUtils());
        MinecraftForge.EVENT_BUS.register(new ParticleLimiter());
        MinecraftForge.EVENT_BUS.register(new EntityCull());
        MinecraftForge.EVENT_BUS.register(new ChunkTweaks());
        MinecraftForge.EVENT_BUS.register(new LightOptimizer());
        MinecraftForge.EVENT_BUS.register(new VoidBlockLagFix());
        MinecraftForge.EVENT_BUS.register(new MinecraftOptimizer());
        MinecraftForge.EVENT_BUS.register(new ZoomHandler());
        MinecraftForge.EVENT_BUS.register(new PerspectiveMod());

        HudPositionManager.load();
        hudManager = new HudManager();

        hudManager.register(
                new FPSHUD(),
                new PingHUD(),
                new PetHud(),
                new RadarHUD(),
                new KeystrokesWasdHud(),
                new KeystrokesLmbHud(),
                new KeystrokesRmbHud(),
                new ArmorStatusHUD()
        );

        MinecraftForge.EVENT_BUS.register(hudManager);
        MinecraftForge.EVENT_BUS.register(new TracerLineRenderer());

        keystrokesManager = new KeystrokesManager();
        MinecraftForge.EVENT_BUS.register(keystrokesManager);

        MinecraftForge.EVENT_BUS.register(new AnnounceMutante());
        MinecraftForge.EVENT_BUS.register(new AnnounceMutanteEvent());
        MinecraftForge.EVENT_BUS.register(new DestroyBlock());
        MinecraftForge.EVENT_BUS.register(new InventoryFullBlock());
        MinecraftForge.EVENT_BUS.register(new AntiCheatCombiner());
        MinecraftForge.EVENT_BUS.register(new ECTPCOmmand());
        MinecraftForge.EVENT_BUS.register(new AdminGui("vitorxp"));
        MinecraftForge.EVENT_BUS.register(new PetMaxBlockChat());
        MinecraftForge.EVENT_BUS.register(new GuiMenuEvent());
        MinecraftForge.EVENT_BUS.register(new InventoryFullNotifier());
        MinecraftForge.EVENT_BUS.register(new InventoryLossLogger());
        MinecraftForge.EVENT_BUS.register(new ChatCommandTracker());
        MinecraftForge.EVENT_BUS.register(new SuspiciousBehaviorDetector());
        MinecraftForge.EVENT_BUS.register(new SellMessageCombiner());
        MinecraftForge.EVENT_BUS.register(new ChatModifier());
        MinecraftForge.EVENT_BUS.register(new Keybinds());
        MinecraftForge.EVENT_BUS.register(new RadarInteractionHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerInspectorHandler());

        ClientCommandHandler.instance.registerCommand(new CommandPetMaxBlock());
        ClientCommandHandler.instance.registerCommand(new CommandInventoryBlock());
        ClientCommandHandler.instance.registerCommand(new CommandMutanteAnnounce());
        ClientCommandHandler.instance.registerCommand(new CommandOpenMenu());
        ClientCommandHandler.instance.registerCommand(new CommandActivate());
        ClientCommandHandler.instance.registerCommand(new CommandEditHud());
        ClientCommandHandler.instance.registerCommand(new CommandTest());
        ClientCommandHandler.instance.registerCommand(new CopyMessageCommand());
        ClientCommandHandler.instance.registerCommand(new AdminCommandStaff());
        ClientCommandHandler.instance.registerCommand(new ECTPCOmmand());
        ClientCommandHandler.instance.registerCommand(new PainelAdminCommand());

        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        settings.fboEnable = true;
        settings.useVbo = true;
        settings.ambientOcclusion = 0;
        settings.clouds = 0;

        try {
            settings.mipmapLevels = 4;
        } catch (Exception ignored) {}

        Minecraft.getMinecraft().renderGlobal.loadRenderers();

        System.out.println("SkyBlockModVX carregado e otimizações aplicadas!");
    }


    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {

    }

//    @SubscribeEvent
//    public void onClientTick(TickEvent.ClientTickEvent event) {
//        if (event.phase == TickEvent.Phase.END) {
//            String state = Minecraft.getMinecraft().theWorld != null ? Minecraft.getMinecraft().theWorld.getWorldInfo().getWorldName() : "No world";
//            String details = "Jogando Minecraft 1.8.9";
//            DiscordRPC.sendActivity(state, details);
//        }
//    }

    @Mod.EventHandler
    public void onShutdown(FMLServerStoppedEvent event) {
        DiscordRPC.stop();
        InventoryLossLogger.saveToFile();
        HudPositionManager.save();
        ConfigManager.save();
    }
}
