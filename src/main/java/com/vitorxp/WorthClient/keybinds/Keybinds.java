package com.vitorxp.WorthClient.keybinds;

import com.vitorxp.WorthClient.manager.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import com.vitorxp.WorthClient.WorthClient;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Keybinds {
    public static KeyBinding openConfig;
    public static KeyBinding openConfigHud;
    public static KeyBinding screenshotKey;
    public static KeyBinding perspectiveM;
    public static KeyBinding ZoomM;
    public static KeyBinding zoomKey;
    private static boolean keysInitialized = false;
    private boolean wasPressed = false;


    public static void init() {
        ConfigManager.load();

        openConfig = new KeyBinding("Abrir menu de Configurações", Keyboard.KEY_K, "WorthClient");
        ClientRegistry.registerKeyBinding(openConfig);

        openConfigHud = new KeyBinding("Abrir menu de Overlays", Keyboard.KEY_RSHIFT, "WorthClient");
        ClientRegistry.registerKeyBinding(openConfigHud);

        screenshotKey = new KeyBinding("Tirar Screenshot", Keyboard.KEY_P, "WorthClient");
        //ClientRegistry.registerKeyBinding(screenshotKey);

        perspectiveM = new KeyBinding("Perspective Mod", WorthClient.KeyPerspective, "WorthClient");
        ClientRegistry.registerKeyBinding(perspectiveM);

        ZoomM = new KeyBinding("Botão do Zoom", WorthClient.KeyZoom, "WorthClient");
        ClientRegistry.registerKeyBinding(ZoomM);
    }

    public static void updatePerspectiveKey(int newKeyCode) {
        if (perspectiveM != null) {
            perspectiveM.setKeyCode(newKeyCode);
            KeyBinding.resetKeyBindingArrayAndHash();
        }
    }

    public static void updateZoomKey(int newKeyCode) {
        if (ZoomM != null) {
            ZoomM.setKeyCode(newKeyCode);
            KeyBinding.resetKeyBindingArrayAndHash();
        }
    }

    public static void disableOptifineZoom() {
        Minecraft mc = Minecraft.getMinecraft();
        for (KeyBinding kb : mc.gameSettings.keyBindings) {
            if (kb.getKeyDescription().equals("of.key.zoom")) {
                if (kb.getKeyCode() == zoomKey.getKeyCode() || kb.getKeyCode() == Keyboard.KEY_C) {
                    kb.setKeyCode(0);
                    KeyBinding.resetKeyBindingArrayAndHash();
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (openConfig.isPressed()) {
            com.vitorxp.WorthClient.WorthClient.pendingOpenMenu = true;
        }
        if (openConfigHud.isPressed()) {
            com.vitorxp.WorthClient.WorthClient.pendingOpenMenuHud = true;
        }
        if (screenshotKey.isPressed()) {
            takeScreenshotAndUpload();
        }

        if (WorthClient.PerspectiveModToggle) {
            if (perspectiveM.isPressed()) {
                WorthClient.perspective = !WorthClient.perspective;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!keysInitialized) {
            if (perspectiveM != null) {
                perspectiveM.setKeyCode(WorthClient.KeyPerspective);
            }

            if (ZoomM != null) {
                ZoomM.setKeyCode(WorthClient.KeyZoom);
            }

            disableOptifineZoom();
            KeyBinding.resetKeyBindingArrayAndHash();
            keysInitialized = true;
        }

        if (!WorthClient.PerspectiveModToggle) {
            boolean isPressed = Keyboard.isKeyDown(perspectiveM.getKeyCode());

            if (isPressed && !wasPressed) {
                WorthClient.perspective = true;
            }

            if (!isPressed && wasPressed) {
                WorthClient.perspective = false;
            }

            wasPressed = isPressed;
        }
    }

    public void takeScreenshotAndUpload() {
        Minecraft mc = Minecraft.getMinecraft();
        File screenshotsDir = new File(mc.mcDataDir, "screenshots");

        if (!screenshotsDir.exists()) screenshotsDir.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        String fileName = sdf.format(new Date()) + ".png";

        File screenshotFile = new File(screenshotsDir, fileName);

        try {
            ScreenShotHelper.saveScreenshot(screenshotsDir, fileName, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§aScreenshot salva, enviando..."));
            Thread.sleep(300);

            uploadScreenshot(screenshotFile);
        } catch (Exception e) {
            e.printStackTrace();
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§cErro ao tirar screenshot."));
        }
    }

    public static String uploadScreenshot(File file) {
        try {
            URL url = new URL("https://0x0.st");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            String boundary = Long.toHexString(System.currentTimeMillis());
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (
                    OutputStream output = connection.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)
            ) {
                String fileName = file.getName();

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
                writer.append("Content-Type: image/png\r\n\r\n").flush();

                Files.copy(file.toPath(), output);
                output.flush();

                writer.append("\r\n").flush();
                writer.append("--").append(boundary).append("--").append("\r\n").flush();
            }

            InputStream response = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(response));

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(br.readLine()), null);

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§aScreenshot enviada: §f" + br.readLine()));
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao enviar print.";
        }
        return "";
    }
}
