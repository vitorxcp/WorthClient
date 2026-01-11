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
import com.vitorxp.WorthClient.handlers.IslandProtectionHandler;
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
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod(modid = "worthclient", name = "WorthClient", version = "1.0.1-alpha", clientSideOnly = true)
public class WorthClient {

    public static final Logger logger = LogManager.getLogger("WorthClient");

    public static boolean openGuiChat = false;
    public static boolean ArmorsOverlays = false;
    public static int KeyPerspective = Keyboard.KEY_LMENU;
    public static boolean WailaMod = false;
    public static boolean enableToggleZoom = false;
    public static int KeyZoom = Keyboard.KEY_C;
    public static boolean PerspectiveStartFront = false;
    public static boolean AutoLoginEnabled = true;
    public static float pixelsThickness = 1.0f;
    public static boolean skin3D = false;
    private final SessionManager sessionManager = new SessionManager();
    private static ServerData lastServerAttempted;
    public static boolean blockPetMessages = true;
    public static boolean blockInventoryMessages = true;
    public static boolean announceZealot = true;
    public static boolean MsgBlockDestroyBlock = true;
    public static boolean petOverlay = true;
    public static boolean timeChangerEnable = false;
    public static float clientTime = 6000.0f;
    public static boolean buildEnabled = false;
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
            if (!configFile.exists()) return;
            List<String> lines = Files.readAllLines(configFile.toPath());
            boolean changed = false;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).replace(" ", "").trim().startsWith("enabled=true")) {
                    lines.set(i, "enabled=false");
                    changed = true;
                }
            }
            if (changed) Files.write(configFile.toPath(), lines);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        LoadingUtils.setCurrentText("Iniciando WorthClient...");
        LoadingUtils.setCurrentProgress(0.05f);
        //WindowUtils.applyWindowStyle();
        PerfConfig.load(e.getSuggestedConfigurationFile());
        VoidLagFixConfig.syncConfig(e.getSuggestedConfigurationFile());
        SSLTrustBypasser.install();
        SSLTrustManager.initialize();
    }

    public static final Map<String, String> pendingPlayersTP = new ConcurrentHashMap<>();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LoadingUtils.setCurrentText("Carregando Módulos...");
        LoadingUtils.setCurrentProgress(0.1f);

        Thread loaderThread = new Thread(() -> {
            try {
                updateProgress("Carregando Contas...", 0.2f);
                AccountManager.loadAccounts();

                updateProgress("Conectando Discord RPC...", 0.3f);
                DiscordRPC.start("1325483160011804754");

                updateProgress("Conectando Socket...", 0.4f);
                com.vitorxp.WorthClient.socket.ClientSocket.connect();

                updateProgress("Lendo Configurações...", 0.5f);
                ActivationManager.init();
                ConfigManager.load();
                HudPositionManager.load();
                Keybinds.init();
                KeystrokesColors.loadColors();
                AutoTextManager.load();
                AutoLoginManager.load();

                updateProgress("Finalizando...", 0.9f);
                Thread.sleep(300);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, "WorthClient-Loader");

        loaderThread.start();

        while (loaderThread.isAlive()) {
            LoadingUtils.renderLoading(null, -1);
        }

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
        MinecraftForge.EVENT_BUS.register(new RadarInteractionHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerInspectorHandler());
        MinecraftForge.EVENT_BUS.register(new AutoLoginHandler());
        MinecraftForge.EVENT_BUS.register(new IslandProtectionHandler());
        MinecraftForge.EVENT_BUS.register(new NotificationManager());

        hudManager = new HudManager();
        hudManager.register(
                new FPSHUD(), new PingHUD(), new PetHud(), new RadarHUD(),
                new KeystrokesWasdHud(), new KeystrokesLmbHud(), new KeystrokesRmbHud(),
                new ArmorStatusHUD(), new LookAtHUD(), new ScoreboardHUD()
        );
        MinecraftForge.EVENT_BUS.register(hudManager);

        registerCommands();

        keystrokesManager = new KeystrokesManager();
        MinecraftForge.EVENT_BUS.register(new TracerLineRenderer());
        MinecraftForge.EVENT_BUS.register(keystrokesManager);
        MinecraftForge.EVENT_BUS.register(new AnnounceMutante());
        MinecraftForge.EVENT_BUS.register(new AnnounceMutanteEvent());
        MinecraftForge.EVENT_BUS.register(new DestroyBlock());
        MinecraftForge.EVENT_BUS.register(new InventoryFullBlock());
        MinecraftForge.EVENT_BUS.register(new AntiCheatCombiner());
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

        optimizeGameSettings();

        LoadingUtils.setCurrentText("Pronto!");
        LoadingUtils.setCurrentProgress(1.0f);

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 300) {
            LoadingUtils.renderLoading(null, -1);
        }
    }

    private void updateProgress(String text, float percent) {
        LoadingUtils.setCurrentText(text);
        LoadingUtils.setCurrentProgress(percent);
    }

    private void registerCommands() {
        ECTPCOmmand ectpCommand = new ECTPCOmmand();
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            public String getCommandName() { return "chatsettings"; }
            public String getCommandUsage(ICommandSender sender) { return "/chatsettings"; }
            public int getRequiredPermissionLevel() { return 0; }
            public void processCommand(ICommandSender sender, String[] args) { WorthClient.openGuiChat = true; }
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
        ClientCommandHandler.instance.registerCommand(ectpCommand);
        ClientCommandHandler.instance.registerCommand(new PainelAdminCommand());
        ClientCommandHandler.instance.registerCommand(new CommandAntiCheatLogs());
        ClientCommandHandler.instance.registerCommand(new CommandBuildIs());
        MinecraftForge.EVENT_BUS.register(ectpCommand);
    }

    private void optimizeGameSettings() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        settings.fboEnable = true;
        settings.useVbo = true;
        settings.ambientOcclusion = 0;
        settings.clouds = 0;
        try { settings.mipmapLevels = 4; } catch (Exception ignored) {}
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    }

    private boolean packsLoaded = false;
    private int tickCounter = 0;

    @SubscribeEvent
    public void onTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();

        if (!packsLoaded && (mc.currentScreen instanceof net.minecraft.client.gui.GuiMainMenu || mc.thePlayer != null)) {
            tickCounter++;

            if (tickCounter > 20) {
                logger.info("Inicialização concluída. Carregando texturas salvas...");
                WorthPackLoader.reloadSavedPacks();
                packsLoaded = true;
            }
        }
    }

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Minecraft.getMinecraft().addScheduledTask(() ->
                sessionManager.saveSession(Minecraft.getMinecraft().getSession())
        );
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            try { NotificationRenderer.render(Minecraft.getMinecraft()); } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.gui instanceof GuiMultiplayer && event.button.id == 1) {
            try {
                Field f = GuiMultiplayer.class.getDeclaredField("field_146801_A");
                f.setAccessible(true);
                lastServerAttempted = (ServerData) f.get(event.gui);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiDisconnected) {
            try {
                GuiDisconnected gd = (GuiDisconnected) event.gui;
                Field f = GuiDisconnected.class.getDeclaredField("message");
                f.setAccessible(true);
                IChatComponent c = (IChatComponent) f.get(gd);
                if (c.getUnformattedText().toLowerCase().contains("authentication servers are currently down")) {
                    if (lastServerAttempted != null) {
                        sessionManager.applyCachedSession();
                        event.setCanceled(true);
                        Minecraft.getMinecraft().displayGuiScreen(new GuiConnecting(null, Minecraft.getMinecraft(), lastServerAttempted));
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    @Mod.EventHandler
    public void onShutdown(FMLServerStoppedEvent event) {
        DiscordRPC.stop();
        InventoryLossLogger.saveToFile();
        HudPositionManager.save();
        ConfigManager.save();
    }
}