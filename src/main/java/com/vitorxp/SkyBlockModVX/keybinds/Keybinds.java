package com.vitorxp.SkyBlockModVX.keybinds;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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
    public static KeyBinding zoomKey;

    public static void init() {
        openConfig = new KeyBinding("Abrir menu de Configurações", Keyboard.KEY_K, "SkyBlockModVX");
        ClientRegistry.registerKeyBinding(openConfig);

        openConfigHud = new KeyBinding("Abrir menu de Overlays", Keyboard.KEY_RSHIFT, "SkyBlockModVX");
        ClientRegistry.registerKeyBinding(openConfigHud);

        screenshotKey = new KeyBinding("Tirar Screenshot", Keyboard.KEY_P, "SkyBlockModVX");
        //ClientRegistry.registerKeyBinding(screenshotKey);

        perspectiveM = new KeyBinding("Perspective Mod", Keyboard.KEY_LMENU, "SkyBlockModVX");
        ClientRegistry.registerKeyBinding(perspectiveM);

        zoomKey = new KeyBinding("Botão de Zoom", Keyboard.KEY_C, "SkyBlockModVX");
        ClientRegistry.registerKeyBinding(zoomKey);

    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (openConfig.isPressed()) {
            SkyBlockMod.pendingOpenMenu = true;
        }
        if (openConfigHud.isPressed()) {
            SkyBlockMod.pendingOpenMenuHud = true;
        }
        if (screenshotKey.isPressed()) {
            takeScreenshotAndUpload();
        }
    }

    private boolean wasPressed = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        boolean isPressed = perspectiveM.isKeyDown();

        if (isPressed && !wasPressed) {
            SkyBlockMod.perspective = true;
        }

        if (!isPressed && wasPressed) {
            SkyBlockMod.perspective = false;
        }

        wasPressed = isPressed;
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
