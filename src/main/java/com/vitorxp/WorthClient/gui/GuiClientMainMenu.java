package com.vitorxp.WorthClient.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import com.vitorxp.WorthClient.gui.utils.AnimationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class GuiClientMainMenu extends GuiScreen {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("worthclient", "textures/gui/Background_3.png");
    private static final ResourceLocation MAIN_LOGO = new ResourceLocation("worthclient", "textures/gui/logo_main.png");
    private static final ResourceLocation ICON_DISCORD = new ResourceLocation("worthclient", "textures/icons/discord.png");
    private static final ResourceLocation ICON_THEME = new ResourceLocation("worthclient", "textures/icons/theme.png");
    private static final ResourceLocation ICON_RELOAD = new ResourceLocation("worthclient", "textures/icons/reload.png");
    private static final ResourceLocation ICON_CLOSE = new ResourceLocation("worthclient", "textures/icons/close.png");
    private long animationStartTime;
    private boolean isOpening, isClosing;
    private final int ANIMATION_DURATION_MS = 600;
    private GuiScreen nextScreen = null;
    private GuiIconButton discordButton;
    private GuiIconButton themeButton;
    private GuiIconButton reloadButton;
    private GuiIconButton closeButton;

    private boolean showThemeSelector = false;
    private float themeSelectorAlpha = 0.0f;

    private String serverMotd = "Carregando informações...";
    private final String SERVER_STATUS_API = "https://api.mcstatus.io/v2/status/java/redeworth.com";

    public enum Theme {
        DARK("Dark", new Color(15, 15, 15, 140), new Color(255, 255, 255), new Color(255, 170, 0)),
        STANDARD("Worth", new Color(30, 20, 10, 130), new Color(255, 220, 50), new Color(255, 215, 0)),
        LIGHT("Light", new Color(245, 245, 245, 160), new Color(40, 40, 40), new Color(255, 140, 0));

        String name;
        Color overlayColor;
        Color textColor;
        Color accentColor;

        Theme(String name, Color overlay, Color text, Color accent) {
            this.name = name;
            this.overlayColor = overlay;
            this.textColor = text;
            this.accentColor = accent;
        }
    }

    public static Theme currentTheme = Theme.DARK;

    @Override
    public void initGui() {
        this.buttonList.clear();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int btnWidth = 180;
        int btnHeight = 26;
        int spacing = 32;
        int startY = centerY - 15;

        this.closeButton = new GuiIconButton(3, this.width - 35, 10, 24, 24, ICON_CLOSE);
        this.buttonList.add(this.closeButton);
        this.buttonList.add(new GuiModernButton(0, centerX - (btnWidth / 2), startY, btnWidth, btnHeight, "Singleplayer", 500L));
        this.buttonList.add(new GuiModernButton(1, centerX - (btnWidth / 2), startY + spacing, btnWidth, btnHeight, "Multiplayer", 600L));
        this.buttonList.add(new GuiModernButton(2, centerX - (btnWidth / 2), startY + spacing * 2, btnWidth, btnHeight, "Opções", 700L));

        int iconSize = 24;
        int iconSpacing = 20;
        int bottomY = this.height - 45
;
        int totalGroupWidth = (iconSize * 3) + (iconSpacing * 2);
        int currentX = centerX - (totalGroupWidth / 2);

        this.reloadButton = new GuiIconButton(4, currentX, bottomY, iconSize, iconSize, ICON_RELOAD);
        this.buttonList.add(this.reloadButton);
        currentX += iconSize + iconSpacing;

        this.discordButton = new GuiIconButton(5, currentX, bottomY, iconSize, iconSize, ICON_DISCORD);
        this.buttonList.add(this.discordButton);
        currentX += iconSize + iconSpacing;

        this.themeButton = new GuiIconButton(7, currentX, bottomY, iconSize, iconSize, ICON_THEME);
        this.buttonList.add(this.themeButton);

        if (serverMotd.equals("Carregando informações...")) {
            fetchServerMotd();
        }

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

        for (GuiButton button : this.buttonList) {
            if (showThemeSelector && themeSelectorAlpha > 0.8f) {
                // Lógica de bloqueio se necessário
            }

            if (button instanceof GuiModernButton) {
                ((GuiModernButton) button).drawButton(this.mc, mouseX, mouseY, uiAlpha);
            } else {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1f, 1f, 1f, uiAlpha);
                button.drawButton(this.mc, mouseX, mouseY);
                GlStateManager.popMatrix();
            }
        }

        this.drawCenteredString(this.fontRendererObj, "WorthClient © 2025 - Developed by vitorxp", this.width / 2, this.height - 15, new Color(150, 150, 150, (int)(255 * uiAlpha)).getRGB());

        if (uiAlpha > 0.9f && (!showThemeSelector || themeSelectorAlpha < 0.1f)) {
            for (GuiButton button : this.buttonList) {
                boolean isHovered = mouseX >= button.xPosition && mouseY >= button.yPosition &&
                        mouseX < button.xPosition + button.width && mouseY < button.yPosition + button.height;

                if (isHovered) {
                    if (button.id == 3) {
                        this.drawHoveringText(Collections.singletonList("Sair do Jogo"), mouseX, mouseY);
                    }
                    if (button.id == 4) {
                        this.drawHoveringText(Collections.singletonList("Reiniciar Texturas"), mouseX, mouseY);
                    }
                    if (button.id == 5) {
                        this.drawHoveringText(Collections.singletonList("Entrar no Discord"), mouseX, mouseY);
                    }
                    if (button.id == 7) {
                        this.drawHoveringText(Collections.singletonList("Selecionar Tema"), mouseX, mouseY);
                    }
                }
            }
        }

        float targetThemeAlpha = showThemeSelector ? 1.0f : 0.0f;
        themeSelectorAlpha = themeSelectorAlpha + (targetThemeAlpha - themeSelectorAlpha) * 0.2f;

        if (themeSelectorAlpha > 0.01f) {
            drawThemeSelector(mouseX, mouseY, themeSelectorAlpha);
        }

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

    private void drawThemeSelector(int mouseX, int mouseY, float alpha) {
        GlStateManager.pushMatrix();
        float scale = 0.9f + (0.1f * alpha);

        int bgAlpha = (int)(150 * alpha);
        drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, bgAlpha).getRGB());

        GlStateManager.translate(this.width / 2.0f, this.height / 2.0f, 0);
        GlStateManager.scale(scale, scale, 1.0f);
        GlStateManager.translate(-(this.width / 2.0f), -(this.height / 2.0f), 0);

        int w = 180;
        int h = 160;
        int x = (this.width / 2) - (w / 2);
        int y = (this.height / 2) - (h / 2);

        int menuBgColor = currentTheme == Theme.LIGHT ? 0xFFF0F0F0 : 0xFF181818;
        int menuBorderColor = currentTheme == Theme.LIGHT ? 0xFFAAAAAA : 0xFF333333;

        int alphaInt = (int)(255 * alpha);

        drawRoundedRect(x, y, w, h, 12, menuBgColor);
        drawRoundedOutline(x, y, w, h, 12, 1.0f, currentTheme.accentColor.getRGB());

        int textColor = currentTheme == Theme.LIGHT ? 0xFF222222 : 0xFFEEEEEE;
        this.drawCenteredString(this.fontRendererObj, "Selecione o Tema", this.width / 2, y + 15, textColor);

        int btnY = y + 45;
        for (Theme theme : Theme.values()) {
            boolean hover = mouseX >= x + 20 && mouseX <= x + w - 20 && mouseY >= btnY && mouseY <= btnY + 28;

            int itemBg;
            if (hover) itemBg = theme.accentColor.getRGB();
            else if (currentTheme == theme) itemBg = new Color(theme.accentColor.getRed(), theme.accentColor.getGreen(), theme.accentColor.getBlue(), 100).getRGB();
            else itemBg = currentTheme == Theme.LIGHT ? 0xFFDDDDDD : 0xFF252525;

            int itemText = (hover || currentTheme == theme || currentTheme != Theme.LIGHT) ? 0xFFFFFFFF : 0xFF333333;

            drawRoundedRect(x + 20, btnY, w - 40, 28, 6, itemBg);
            this.drawCenteredString(this.fontRendererObj, theme.name, this.width / 2, btnY + 10, itemText);

            btnY += 35;
        }
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isOpening || isClosing) return;

        if (showThemeSelector && themeSelectorAlpha > 0.8f) {
            int w = 180;
            int h = 160;
            int x = (this.width / 2) - (w / 2);
            int y = (this.height / 2) - (h / 2);

            int btnY = y + 45;
            for (Theme theme : Theme.values()) {
                if (mouseX >= x + 20 && mouseX <= x + w - 20 && mouseY >= btnY && mouseY <= btnY + 28) {
                    currentTheme = theme;
                    this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    showThemeSelector = false;
                    return;
                }
                btnY += 35;
            }

            if (mouseX < x || mouseX > x + w || mouseY < y || mouseY > y + h) {
                showThemeSelector = false;
            }
            return;
        }

        if (!showThemeSelector) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } else if (mouseX < (this.width/2 - 90) || mouseX > (this.width/2 + 90) || mouseY < (this.height/2 - 80) || mouseY > (this.height/2 + 80)) {
            showThemeSelector = false;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (isClosing || isOpening || showThemeSelector) return;

        switch (button.id) {
            case 0: triggerExitAnimation(new net.minecraft.client.gui.GuiSelectWorld(this)); break;
            case 1: triggerExitAnimation(new GuiMultiplayerCustom(this)); break;
            case 2: triggerExitAnimation(new net.minecraft.client.gui.GuiOptions(this, this.mc.gameSettings)); break;
            case 3: triggerExitAnimation(null); break;
            case 4: this.mc.refreshResources(); break;
            case 5: openDiscord("https://discord.gg/VWHvq9zpeV"); break;
            case 7: showThemeSelector = true; break;
        }
    }

    public void drawDefaultBackground() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);

        drawRect(0, 0, this.width, this.height, currentTheme.overlayColor.getRGB());
    }

    private void drawLogo(float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

        float yOffset = (float) Math.sin(System.currentTimeMillis() / 1200.0F) * 6.0F;

        int logoWidth = 235;
        int logoHeight = 48;
        int logoX = this.width / 2 - logoWidth / 2;
        int logoY = (this.height / 3) - logoHeight - 10;

        this.mc.getTextureManager().bindTexture(MAIN_LOGO);
        drawModalRectWithCustomSizedTexture(logoX, (int)(logoY + yOffset), 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);
        GlStateManager.popMatrix();
    }

    private void drawMotd(float alpha) {
        if (serverMotd != null && !serverMotd.isEmpty()) {
            int logoHeight = 48;
            int logoY = (this.height / 3) - logoHeight - 10;
            int motdY = logoY + logoHeight + 10;

            String[] lines = serverMotd.split("\n");

            int color = currentTheme.textColor.getRGB();
            int alphaInt = (int)(alpha * 255);
            color = (color & 0x00FFFFFF) | (alphaInt << 24);

            for (String line : lines) {
                int lineWidth = this.fontRendererObj.getStringWidth(line);
                int lineX = this.width / 2 - lineWidth / 2;
                this.fontRendererObj.drawStringWithShadow(line, lineX, motdY, color);
                motdY += this.fontRendererObj.FONT_HEIGHT + 4;
            }
        }
    }

    private void openDiscord(String url) {
        try { Desktop.getDesktop().browse(new URI(url)); }
        catch (IOException | URISyntaxException e) { e.printStackTrace(); }
    }

    private void drawSlidingBarsTransition(float progress) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        Color goldDark = new Color(139, 105, 20, 255);
        Color goldLight = new Color(255, 215, 0, 255);

        float w = this.width;
        float h = this.height;

        drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, (int)(255 * progress)).getRGB());

        int numBars = 3;
        float barHeight = h / numBars;
        float totalWidthToCover = w + 200;

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

    private void fetchServerMotd() {
        CompletableFuture.runAsync(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(SERVER_STATUS_API);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestMethod("GET");

                String json;
                try (InputStream inputStream = connection.getInputStream()) {
                    json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                }

                JsonObject root = new JsonParser().parse(json).getAsJsonObject();
                if (root.get("online").getAsBoolean()) {
                    if (root.has("motd")) {
                        JsonObject motdObj = root.getAsJsonObject("motd");
                        if (motdObj.has("raw")) {
                            JsonElement rawElement = motdObj.get("raw");
                            if (rawElement.isJsonArray()) {
                                JsonArray rawArray = rawElement.getAsJsonArray();
                                StringBuilder motdBuilder = new StringBuilder();
                                for (int i = 0; i < rawArray.size(); i++) {
                                    motdBuilder.append(rawArray.get(i).getAsString());
                                    if (i < rawArray.size() - 1) motdBuilder.append("\n");
                                }
                                serverMotd = motdBuilder.toString();
                            } else {
                                serverMotd = rawElement.getAsString();
                            }
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
                serverMotd = "§7Bem-vindo ao WorthClient!";
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (int i = 0; i <= 90; i += 1)
            worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 90; i <= 180; i += 1)
            worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 180; i <= 270; i += 1)
            worldrenderer.pos(x + radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 270; i <= 360; i += 1)
            worldrenderer.pos(x + radius + Math.sin(Math.toRadians(i)) * radius, y + radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawRoundedOutline(float x, float y, float width, float height, float radius, float thickness, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        GL11.glLineWidth(thickness);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 0; i <= 90; i += 1)
            worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 90; i <= 180; i += 1)
            worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 180; i <= 270; i += 1)
            worldrenderer.pos(x + radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 270; i <= 360; i += 1)
            worldrenderer.pos(x + radius + Math.sin(Math.toRadians(i)) * radius, y + radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static class GuiIconButton extends GuiButton {
        private final ResourceLocation icon;
        private float hoverScale = 1.0f;

        public GuiIconButton(int buttonId, int x, int y, int width, int height, ResourceLocation icon) {
            super(buttonId, x, y, width, height, "");
            this.icon = icon;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                this.hovered = mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;

                float targetScale = hovered ? 1.1f : 1.0f;
                hoverScale = hoverScale + (targetScale - hoverScale) * 0.15f;

                Color accent = currentTheme.accentColor;

                int bgColor = 0x40000000;
                int borderColor = 0x20FFFFFF;

                if (hovered) {
                    bgColor = 0x80000000;
                    borderColor = accent.getRGB();
                }

                drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, 6.0f, bgColor);
                drawRoundedOutline(this.xPosition, this.yPosition, this.width, this.height, 6.0f, 1.0f, borderColor);

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                float centerX = this.xPosition + this.width / 2.0f;
                float centerY = this.yPosition + this.height / 2.0f;
                GlStateManager.translate(centerX, centerY, 0);
                GlStateManager.scale(hoverScale, hoverScale, 1.0f);
                GlStateManager.translate(-centerX, -centerY, 0);

                if (hovered) {
                    GlStateManager.color(accent.getRed()/255f, accent.getGreen()/255f, accent.getBlue()/255f, 1.0f);
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    GlStateManager.color(0.8f, 0.8f, 0.8f, 0.7f);
                }

                mc.getTextureManager().bindTexture(icon);
                int padding = 5;
                drawModalRectWithCustomSizedTexture(
                        this.xPosition + padding,
                        this.yPosition + padding,
                        0, 0,
                        this.width - (padding * 2),
                        this.height - (padding * 2),
                        this.width - (padding * 2),
                        this.height - (padding * 2)
                );

                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }
}