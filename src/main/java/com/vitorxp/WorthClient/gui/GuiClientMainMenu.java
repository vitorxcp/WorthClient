package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import com.vitorxp.WorthClient.gui.utils.AnimationUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GuiClientMainMenu extends GuiScreen {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("worthclient", "textures/gui/Background_3.png");
    private static final ResourceLocation MAIN_LOGO = new ResourceLocation("worthclient", "textures/gui/logo_main.png");

    private long animationStartTime;
    private boolean isOpening, isClosing;
    private final int ANIMATION_DURATION_MS = 1000;
    private GuiScreen nextScreen = null;

    private String serverMotd = "Carregando informações...";
    private final String SERVER_STATUS_API = "https://api.mcsrvstat.us/2/redeworth.com";

    @Override
    public void initGui() {
        this.buttonList.clear();

        int buttonY = this.height / 2 - 10;
        int buttonSpacing = 30;

        this.buttonList.add(new GuiModernButton(0, this.width / 2 - 100, buttonY, "Seus Mundos", 500L));
        this.buttonList.add(new GuiModernButton(1, this.width / 2 - 100, buttonY + buttonSpacing, "Servidores", 600L));
        this.buttonList.add(new GuiModernButton(2, this.width / 2 - 100, buttonY + buttonSpacing * 2, "Opções", 700L));
        this.buttonList.add(new GuiModernButton(3, this.width / 2 - 100, this.height - 40, 100, 20, "Sair", 800L));
        this.buttonList.add(new GuiModernButton(4, this.width / 2 + 10, this.height - 40, 100, 20, "Reiniciar Texturas", 800L));
        this.buttonList.add(new GuiModernButton(5, this.width - 110, 10, 100, 20, "Discord", 900L));
        this.buttonList.add(new GuiModernButton(6, 10, 10, 100, 20, "Contas", 600L));

        fetchServerMotd();

        this.isOpening = true;
        this.isClosing = false;
        this.animationStartTime = System.currentTimeMillis();
    }

    void triggerExitAnimation(GuiScreen screenToOpen) {
        if (!this.isClosing) {
            this.isClosing = true;
            this.isOpening = false;
            this.animationStartTime = System.currentTimeMillis();
            this.nextScreen = screenToOpen;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float progress = 0;
        if (isOpening || isClosing) {
            long elapsedTime = System.currentTimeMillis() - this.animationStartTime;
            progress = Math.min(1.0f, (float)elapsedTime / (float)this.ANIMATION_DURATION_MS);
        }
        float easedProgress = AnimationUtil.easeOutCubic(progress);

        drawDefaultBackground();

        float uiAlpha = 1.0f;
        if (isOpening) uiAlpha = easedProgress;
        if (isClosing) uiAlpha = 1.0f - easedProgress;

        uiAlpha = Math.max(0.0f, uiAlpha);

        drawLogo(uiAlpha);
        drawMotd(uiAlpha);

        for (net.minecraft.client.gui.GuiButton button : this.buttonList) {
            if (isClosing) {
                if (button instanceof GuiModernButton) {
                    ((GuiModernButton) button).drawButton(this.mc, mouseX, mouseY, uiAlpha);
                } else {
                    button.drawButton(this.mc, mouseX, mouseY);
                }
            } else {
                button.drawButton(this.mc, mouseX, mouseY);
            }
        }
        this.drawCenteredString(this.fontRendererObj, "Criado por: vitorxp", this.width / 2, this.height - 15, new Color(0.33f, 0.33f, 0.33f, uiAlpha).getRGB());

        if (isOpening || isClosing) {
            float transitionEffectProgress = isOpening ? 1.0f - easedProgress : easedProgress;
            drawSlidingBarsTransition(transitionEffectProgress);

            if (progress >= 1.0f) {
                if (isOpening) isOpening = false;
                if (isClosing) {
                    if (this.nextScreen == null) this.mc.shutdown();
                    else this.mc.displayGuiScreen(this.nextScreen);
                }
            }
        }
    }

    private void drawSlidingBarsTransition(float progress) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        Color goldDark = new Color(139, 105, 20, 255);
        Color goldMid = new Color(218, 165, 32, 255);
        Color goldLight = new Color(255, 215, 0, 255);
        Color fadeColor = new Color(0, 0, 0, 255);

        float w = this.width;
        float h = this.height;

        drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, (int)(255 * progress)).getRGB());

        int numBars = 3;
        float barHeight = h / numBars;
        float totalWidthToCover = w + 100;

        for (int i = 0; i < numBars; i++) {
            float startY = barHeight * i;
            float endY = barHeight * (i + 1);

            float offset = (totalWidthToCover * progress);

            float x1_0 = (i % 2 == 0) ? -totalWidthToCover + offset : totalWidthToCover - offset;
            float x2_0 = x1_0 + totalWidthToCover;

            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            addVertexWithColor(worldrenderer, x1_0, startY, goldDark);
            addVertexWithColor(worldrenderer, x2_0, startY, goldLight);
            addVertexWithColor(worldrenderer, x2_0, endY, goldLight);
            addVertexWithColor(worldrenderer, x1_0, endY, goldDark);
            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void addVertexWithColor(WorldRenderer wr, float x, float y, Color c) {
        wr.pos(x, y, 0).color(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, c.getAlpha() / 255.0f).endVertex();
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
        if (isClosing || isOpening) return;

        switch (button.id) {
            case 0: triggerExitAnimation(new net.minecraft.client.gui.GuiSelectWorld(this)); break;
            case 1: triggerExitAnimation(new net.minecraft.client.gui.GuiMultiplayer(this)); break;
            case 2: triggerExitAnimation(new net.minecraft.client.gui.GuiOptions(this, this.mc.gameSettings)); break;
            case 3: triggerExitAnimation(null); break;
            case 4: this.mc.refreshResources(); break;
            case 5: openDiscord("https://discord.gg/VWHvq9zpeV"); break;
            case 6: this.mc.displayGuiScreen(new GuiAccountManager(this)); break;
        }
    }

    public void drawDefaultBackground() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);
        drawRect(0, 0, this.width, this.height, new Color(10, 10, 15, 100).getRGB());
    }

    private void drawLogo(float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        float yOffset = (float) Math.sin(System.currentTimeMillis() / 800.0F) * 4.0F;
        int logoWidth = 470/2;
        int logoHeight = 97/2;
        int logoX = this.width / 2 - logoWidth / 2;
        int logoY = this.height / 4 - logoHeight / 2 - 20;
        this.mc.getTextureManager().bindTexture(MAIN_LOGO);
        drawModalRectWithCustomSizedTexture(logoX, (int)(logoY + yOffset), 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);
        GlStateManager.popMatrix();
    }

    private void drawMotd(float alpha) {
        if (serverMotd != null && !serverMotd.isEmpty()) {
            int logoHeight = 97/2;
            int logoY = this.height / 4 - logoHeight / 2 - 20;
            String[] lines = serverMotd.split("\n");
            int motdY = logoY + logoHeight + 10;
            int color = new Color(1.0f, 1.0f, 1.0f, alpha).getRGB();

            for (String line : lines) {
                int lineWidth = this.fontRendererObj.getStringWidth(line);
                int lineX = this.width / 2 - lineWidth / 2;
                this.fontRendererObj.drawStringWithShadow(line, lineX, motdY, color);
                motdY += this.fontRendererObj.FONT_HEIGHT + 2;
            }
        }
    }

    private void openDiscord(String url) {
        try { Desktop.getDesktop().browse(new URI(url)); }
        catch (IOException | URISyntaxException e) { e.printStackTrace(); }
    }

    private void fetchServerMotd() {
        CompletableFuture.runAsync(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(SERVER_STATUS_API);
                connection = (HttpURLConnection) url.openConnection();

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                connection.setRequestMethod("GET");
                connection.connect();

                String json;
                try (InputStream inputStream = connection.getInputStream()) {
                    json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                }

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
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}