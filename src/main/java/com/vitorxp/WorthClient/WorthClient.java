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
import com.vitorxp.WorthClient.handlers.PlayerInspectorHandler;
import com.vitorxp.WorthClient.handlers.RadarInteractionHandler;
import com.vitorxp.WorthClient.hud.*;
import com.vitorxp.WorthClient.keybinds.Keybinds;
import com.vitorxp.WorthClient.logger.InventoryLossLogger;
import com.vitorxp.WorthClient.manager.*;
import com.vitorxp.WorthClient.optimization.*;

import com.vitorxp.WorthClient.rpc.DiscordRPC;
import com.vitorxp.WorthClient.utils.PerspectiveMod;
import com.vitorxp.WorthClient.utils.SSLTrustBypasser;
import com.vitorxp.WorthClient.utils.SSLTrustManager;
import com.vitorxp.WorthClient.utils.ZoomHandler;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod(modid = "worthclient", name = "WorthClient", version = "1.0.1-alpha", dependencies = "after:optifine", clientSideOnly = true)
public class WorthClient {
    public static boolean openGuiChat = false;
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

    public static boolean modoOfflineAtivo = false;

    public static HudManager hudManager;
    public static KeystrokesManager keystrokesManager;

    public static boolean guiEditorAdmin = false;
    public static boolean GuiAdminazw = false;
    public static String nameArsAdmin;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        SSLTrustBypasser.install();
        SSLTrustManager.initialize();
        PerfConfig.load(e.getSuggestedConfigurationFile());

        File configFile = e.getSuggestedConfigurationFile();
        VoidLagFixConfig.syncConfig(configFile);
    }

    public static final Map<String, String> pendingPlayersTP = new ConcurrentHashMap<>();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        System.out.println("WorthClient ativando...");

        AccountManager.loadAccounts();

        String clientId = "1325483160011804754";
        DiscordRPC.start(clientId);

        ActivationManager.init();
        ConfigManager.load();
        HudPositionManager.load();
        Keybinds.init();
        KeystrokesColors.loadColors();

        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(new com.vitorxp.WorthClient.WindowUtils());
        MinecraftForge.EVENT_BUS.register(new VoidBlockLagFix());
        MinecraftForge.EVENT_BUS.register(new ParticleLimiter());
        MinecraftForge.EVENT_BUS.register(new EntityCull());
        MinecraftForge.EVENT_BUS.register(new ChunkTweaks());
        MinecraftForge.EVENT_BUS.register(new LightOptimizer());
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

        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            public String getCommandName() { return "chatsettings"; }
            public String getCommandUsage(ICommandSender sender) { return "/chatsettings"; }
            public int getRequiredPermissionLevel() { return 0; }
            public void processCommand(ICommandSender sender, String[] args) {
                WorthClient.openGuiChat = true;
            }
        });

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

        System.out.println("WorthClient carregado e otimizações aplicadas!");
    }


    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {

    }

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
        System.out.println("Conexão bem-sucedida. Salvando sessão válida...");
        Session currentSession = Minecraft.getMinecraft().getSession();
        sessionManager.saveSession(currentSession);
        });
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.gui instanceof GuiMultiplayer) {
            if (event.button.id == 1) {
                System.out.println("Botão 'Entrar no Servidor' clicado. Capturando IP...");
                GuiMultiplayer multiplayerScreen = (GuiMultiplayer) event.gui;
                try {
                    Field selectedServerField = GuiMultiplayer.class.getDeclaredField("field_146801_A");
                    selectedServerField.setAccessible(true);
                    lastServerAttempted = (ServerData) selectedServerField.get(multiplayerScreen);

                    if (lastServerAttempted != null) {
                        System.out.println("SUCESSO ao capturar no clique! Servidor: " + lastServerAttempted.serverName);
                    }
                } catch (Exception e) {
                    System.err.println("Falha ao capturar o servidor selecionado na GuiMultiplayer.");
                    e.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
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
                e.printStackTrace();
                return;
            }

            if (reason.toLowerCase().contains("authentication servers are currently down")) {
                System.out.println("Detectado erro de servidores de autenticação!");

                if (lastServerAttempted != null) {
                    System.out.println("Tentando reconexão com sessão em cache para: " + lastServerAttempted.serverName);
                    sessionManager.applyCachedSession();
                    event.setCanceled(true);
                    Minecraft.getMinecraft().displayGuiScreen(new GuiConnecting(null, Minecraft.getMinecraft(), lastServerAttempted));
                } else {
                    System.err.println("Erro de autenticação, mas nenhum servidor foi capturado a tempo.");
                }
            }
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
