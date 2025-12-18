package com.vitorxp.WorthClient;

import com.vitorxp.WorthClient.account.AccountManager;
import com.vitorxp.WorthClient.anticheat.SuspiciousBehaviorDetector;
import com.vitorxp.WorthClient.chat.*;
import com.vitorxp.WorthClient.commands.*;
import com.vitorxp.WorthClient.config.KeystrokesColors;
import com.vitorxp.WorthClient.config.PerfConfig;
import com.vitorxp.WorthClient.config.VoidLagFixConfig;
import com.vitorxp.WorthClient.events.AnnounceMutanteEvent;
import com.vitorxp.WorthClient.events.GuiMenuEvent;
import com.vitorxp.WorthClient.gui.AdminGui;
import com.vitorxp.WorthClient.gui.utils.NotificationRenderer;
import com.vitorxp.WorthClient.handlers.PlayerInspectorHandler;
import com.vitorxp.WorthClient.handlers.RadarInteractionHandler;
import com.vitorxp.WorthClient.hud.*;
import com.vitorxp.WorthClient.keybinds.Keybinds;
import com.vitorxp.WorthClient.logger.InventoryLossLogger;
import com.vitorxp.WorthClient.manager.*;
import com.vitorxp.WorthClient.optimization.*;
import com.vitorxp.WorthClient.rpc.DiscordRPC;
import com.vitorxp.WorthClient.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Session;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod(modid = "worthclient", name = "WorthClient", version = "1.0.1-alpha", dependencies = "after:optifine", clientSideOnly = true)
public class WorthClient {

    public static final Logger logger = LogManager.getLogger("WorthClient");

    private static boolean hasLoadedTextures = false;
    public static boolean openGuiChat = false;
    public static boolean ArmorsOverlays = false;
    public static int KeyPerspective = Keyboard.KEY_LMENU;
    public static boolean WailaMod = false;
    public static boolean enableToggleZoom = false;
    public static int KeyZoom = Keyboard.KEY_C;
    public static boolean PerspectiveStartFront = false;
    public static boolean AutoLoginEnabled = true;
    private final SessionManager sessionManager = new SessionManager();
    private static ServerData lastServerAttempted;
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
    public static boolean GuiAdminPainel = false;
    public static boolean pendingOpenMenu = false;
    public static boolean pendingOpenMenuHud = false;
    public static boolean perspective = false;
    public static HudManager hudManager;
    public static KeystrokesManager keystrokesManager;
    public static boolean GuiAdminazw = false;
    public static String nameArsAdmin;

    static {
        disableForgeSplash();
    }

    private static void disableForgeSplash() {
        try {
            File configFile = new File(Minecraft.getMinecraft().mcDataDir, "config/splash.properties");
            if (!configFile.exists()) {
                return;
            }
            java.util.List<String> lines = java.nio.file.Files.readAllLines(configFile.toPath());
            boolean changed = false;
            for (int i = 0; i < lines.size(); i++) {
                String originalLine = lines.get(i);
                String cleanLine = originalLine.replace(" ", "").trim();

                if (cleanLine.startsWith("enabled=true")) {
                    lines.set(i, "enabled=false");
                    changed = true;
                }
            }
            if (changed) {
                java.nio.file.Files.write(configFile.toPath(), lines);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger.info("==========================================");
        logger.info("Iniciando WorthClient (Pré-Inicialização)");
        logger.info("Carregando configurações de performance...");
        PerfConfig.load(e.getSuggestedConfigurationFile());
        logger.info("Sincronizando configurações do VoidLagFix...");
        File configFile = e.getSuggestedConfigurationFile();
        VoidLagFixConfig.syncConfig(configFile);
        logger.info("Instalando utilitários de janela e SSL...");
        MinecraftForge.EVENT_BUS.register(new com.vitorxp.WorthClient.WindowUtils());
        SSLTrustBypasser.install();
        SSLTrustManager.initialize();
        logger.info("Pré-inicialização concluída.");
    }

    public static final Map<String, String> pendingPlayersTP = new ConcurrentHashMap<>();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("==========================================");
        logger.info("Iniciando WorthClient (Carregamento Principal)");
        logger.info("Carregando Gerenciador de Contas...");
        AccountManager.loadAccounts();
        logger.info("Iniciando Discord Rich Presence...");
        String clientId = "1325483160011804754";
        DiscordRPC.start(clientId);
        logger.info("Carregando Gerenciadores (Config, Hud, Keys)...");
        ActivationManager.init();
        ConfigManager.load();
        HudPositionManager.load();
        Keybinds.init();
        KeystrokesColors.loadColors();
        AutoTextManager.load();
        AutoLoginManager.load();
        logger.info("Registrando Eventos e Otimizações...");
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new VoidBlockLagFix());
        MinecraftForge.EVENT_BUS.register(new ParticleLimiter());
        MinecraftForge.EVENT_BUS.register(new EntityCull());
        MinecraftForge.EVENT_BUS.register(new ChunkTweaks());
        MinecraftForge.EVENT_BUS.register(new LightOptimizer());
        MinecraftForge.EVENT_BUS.register(new MinecraftOptimizer());
        MinecraftForge.EVENT_BUS.register(new ZoomHandler());
        MinecraftForge.EVENT_BUS.register(new AutoTextHandler());
        MinecraftForge.EVENT_BUS.register(new PerspectiveMod());
        logger.info("Registrando Elementos da HUD...");
        hudManager = new HudManager();
        hudManager.register(
                new FPSHUD(),
                new PingHUD(),
                new PetHud(),
                new RadarHUD(),
                new KeystrokesWasdHud(),
                new KeystrokesLmbHud(),
                new KeystrokesRmbHud(),
                new ArmorStatusHUD(),
                new LookAtHUD(),
                new ScoreboardHUD()
        );
        MinecraftForge.EVENT_BUS.register(hudManager);

        logger.info("Registrando Comandos...");
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            public String getCommandName() { return "chatsettings"; }
            public String getCommandUsage(ICommandSender sender) { return "/chatsettings"; }
            public int getRequiredPermissionLevel() { return 0; }
            public void processCommand(ICommandSender sender, String[] args) {
                WorthClient.openGuiChat = true;
            }
        });
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
        ClientCommandHandler.instance.registerCommand(new CommandAntiCheatLogs());

        logger.info("Registrando módulos extras (Keystrokes, Tracer, Anticheat)...");
        keystrokesManager = new KeystrokesManager();
        MinecraftForge.EVENT_BUS.register(new TracerLineRenderer());
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
        MinecraftForge.EVENT_BUS.register(new AutoLoginHandler());
        logger.info("Conectando ao Socket do Cliente...");
        com.vitorxp.WorthClient.socket.ClientSocket.connect();
        logger.info("Aplicando configurações de renderização (FBO/VBO/Mipmap)...");
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        settings.fboEnable = true;
        settings.useVbo = true;
        settings.ambientOcclusion = 0;
        settings.clouds = 0;
        try {
            settings.mipmapLevels = 4;
        } catch (Exception ignored) {}
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
        logger.info("WorthClient carregado e pronto!");
        logger.info("==========================================");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        logger.info("[Post-Init] Verificando integridade dos módulos...");
    }

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        logger.info("Conectado ao servidor. Salvando sessão...");
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Session currentSession = Minecraft.getMinecraft().getSession();
            sessionManager.saveSession(currentSession);
        });
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.displayWidth == 0 || mc.displayHeight == 0) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        try {
            NotificationRenderer.render(mc);
        } catch (Exception e) {
            logger.error("Erro crítico ao renderizar notificação na HUD", e);
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.gui instanceof GuiMultiplayer) {
            if (event.button.id == 1) {
                GuiMultiplayer multiplayerScreen = (GuiMultiplayer) event.gui;
                try {
                    Field selectedServerField = GuiMultiplayer.class.getDeclaredField("field_146801_A");
                    selectedServerField.setAccessible(true);
                    lastServerAttempted = (ServerData) selectedServerField.get(multiplayerScreen);
                } catch (Exception e) {
                    logger.error("Erro ao capturar dados do servidor na GuiMultiplayer", e);
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!hasLoadedTextures && event.gui instanceof GuiMainMenu) {
            hasLoadedTextures = true;
            logger.info("[WorthClient] Menu Principal detectado! Otimizando memória antes de carregar...");
            System.gc();
            WorthPackLoader.reloadSavedPacks();
            System.gc();
        }

        if (event.gui instanceof GuiDisconnected) {
            GuiDisconnected disconnectedScreen = (GuiDisconnected) event.gui;
            String reason = "";
            try {
                Field messageField;
                try {
                    messageField = GuiDisconnected.class.getDeclaredField("message");
                } catch (NoSuchFieldException e) {
                    messageField = GuiDisconnected.class.getDeclaredField("field_146304_f");
                }
                messageField.setAccessible(true);
                IChatComponent messageComponent = (IChatComponent) messageField.get(disconnectedScreen);
                reason = messageComponent.getUnformattedText();
            } catch (Exception e) {
                logger.error("Erro ao ler motivo de desconexão", e);
                return;
            }

            if (reason.toLowerCase().contains("authentication servers are currently down")) {
                logger.warn("Falha na autenticação detectada. Tentando reconexão automática com sessão em cache...");
                if (lastServerAttempted != null) {
                    sessionManager.applyCachedSession();
                    event.setCanceled(true);
                    Minecraft.getMinecraft().displayGuiScreen(new GuiConnecting(null, Minecraft.getMinecraft(), lastServerAttempted));
                }
            }
        }
    }

    @Mod.EventHandler
    public void onShutdown(FMLServerStoppedEvent event) {
        logger.info("==========================================");
        logger.info("Encerrando WorthClient...");
        logger.info("Parando Discord RPC...");
        DiscordRPC.stop();
        logger.info("Salvando logs de inventário...");
        InventoryLossLogger.saveToFile();
        logger.info("Salvando posições da HUD e configurações...");
        HudPositionManager.save();
        ConfigManager.save();
        logger.info("Cliente finalizado com sucesso.");
        logger.info("==========================================");
    }
}