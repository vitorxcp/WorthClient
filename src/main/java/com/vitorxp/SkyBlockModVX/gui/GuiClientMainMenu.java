package com.vitorxp.SkyBlockModVX.gui;

import com.vitorxp.SkyBlockModVX.gui.button.GuiModernButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GuiClientMainMenu extends GuiScreen {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("skyblockmodvx", "textures/gui/Background_3.png");
    private static final ResourceLocation MAIN_LOGO = new ResourceLocation("skyblockmodvx", "textures/gui/logo_main.png");

    private long startTime;

    private boolean isExiting = false;
    private long exitStartTime = 0L;
    private final int FADE_DURATION_MS = 1000;

    private String serverMotd = "Carregando informações...";
    private final String SERVER_STATUS_API = "https://api.mcsrvstat.us/2/redeworth.com";

    @Override
    public void initGui() {
        this.startTime = System.currentTimeMillis();
        this.buttonList.clear();

        int buttonY = this.height / 2 - 10;
        int buttonSpacing = 30;

        this.buttonList.add(new GuiModernButton(0, this.width / 2 - 100, buttonY, "Seus Mundos", 200L));
        this.buttonList.add(new GuiModernButton(1, this.width / 2 - 100, buttonY + buttonSpacing, "Servidores", 300L));
        this.buttonList.add(new GuiModernButton(2, this.width / 2 - 100, buttonY + buttonSpacing * 2, "Opções", 400L));
        this.buttonList.add(new GuiModernButton(3, this.width / 2 - 100, this.height - 40, 100, 20, "Sair", 500L));
        this.buttonList.add(new GuiModernButton(4, this.width / 2 + 10, this.height - 40, 100, 20, "Reiniciar Texturas", 500L));

        this.buttonList.add(new GuiModernButton(5, this.width - 110, 10, 100, 20, "Discord", 600L));

        fetchServerMotd();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color(2.0F, 2.0F, 2.0F, 2.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);

        drawRect(0, 0, this.width, this.height, new Color(10, 10, 15, 100).getRGB());

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        float yOffset = (float) Math.sin(System.currentTimeMillis() / 800.0F) * 4.0F;

        int logoWidth = 470/2;
        int logoHeight = 97/2;
        int logoX = this.width / 2 - logoWidth / 2;
        int logoY = this.height / 4 - logoHeight / 2 - 20;

        this.mc.getTextureManager().bindTexture(MAIN_LOGO);
        drawModalRectWithCustomSizedTexture(logoX, (int)(logoY + yOffset), 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        if (serverMotd != null && !serverMotd.isEmpty()) {
            String[] lines = serverMotd.split("\n");
            int motdY = logoY + logoHeight + 10;

            for (String line : lines) {
                int lineWidth = this.fontRendererObj.getStringWidth(line);
                int lineX = this.width / 2 - lineWidth / 2;
                this.fontRendererObj.drawStringWithShadow(line, lineX, motdY, 0xFFFFFF);
                motdY += this.fontRendererObj.FONT_HEIGHT + 2;
            }
        }
        for (net.minecraft.client.gui.GuiButton button : this.buttonList) {
            button.drawButton(this.mc, mouseX, mouseY);
        }

        this.drawCenteredString(this.fontRendererObj, "Criado por: vitorxp", this.width / 2, this.height - 15, 0x555555);

        if (this.isExiting) {
            long elapsedTime = System.currentTimeMillis() - this.exitStartTime;
            float progress = (float)elapsedTime / (float)this.FADE_DURATION_MS;
            progress = Math.min(1.0f, progress);

            int alpha = (int)(progress * 255);

            drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, alpha).getRGB());

            if (progress >= 1.0f) {
                this.mc.shutdown();
            }
        }
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
        if (this.isExiting) return;

        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiSelectWorld(this));
                break;
            case 1:
                this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiMultiplayer(this));
                break;
            case 2:
                this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiOptions(this, this.mc.gameSettings));
                break;
            case 3:
                this.isExiting = true;
                this.exitStartTime = System.currentTimeMillis();
                break;
            case 4:
                this.mc.refreshResources();
                break;
            case 5:
                openDiscord("https://discord.gg/VWHvq9zpeV");
                break;
        }
    }

    private void openDiscord(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void fetchServerMotd() {
        CompletableFuture.runAsync(() -> {
            try {
                String json = IOUtils.toString(new URL(SERVER_STATUS_API), StandardCharsets.UTF_8);

                JsonObject root = new JsonParser().parse(json).getAsJsonObject();

                if (root.get("online").getAsBoolean()) {
                    if (root.has("motd")) {
                        JsonObject motdObj = root.getAsJsonObject("motd");

                        if (motdObj.has("raw")) {
                            JsonArray rawArray = motdObj.getAsJsonArray("raw");

                            StringBuilder motdBuilder = new StringBuilder();
                            for (int i = 0; i < rawArray.size(); i++) {
                                String line = rawArray.get(i).getAsString();
                                motdBuilder.append(line);
                                if (i < rawArray.size() - 1) {
                                    motdBuilder.append("\n");
                                }
                            }
                            serverMotd = motdBuilder.toString();
                        } else {
                            serverMotd = "§aServidor online!";
                        }
                    } else {
                        serverMotd = "§aServidor online!";
                    }
                } else {
                    serverMotd = "§cServidor offline!";
                }
            } catch (Exception e) {
                serverMotd = "§cErro ao carregar MOTD!";
                e.printStackTrace();
            }
        });
    }
}
