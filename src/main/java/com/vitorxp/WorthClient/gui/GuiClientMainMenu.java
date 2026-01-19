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

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class GuiClientMainMenu extends GuiScreen {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("worthclient", "textures/gui/Background_3.png");
    private static final ResourceLocation MAIN_LOGO = new ResourceLocation("worthclient", "textures/gui/logo_main.png");
    private static final ResourceLocation ICON_DISCORD = new ResourceLocation("worthclient", "textures/icons/discord.png");
    private static final ResourceLocation ICON_THEME = new ResourceLocation("worthclient", "textures/icons/theme.png");
    private static final ResourceLocation ICON_RELOAD = new ResourceLocation("worthclient", "textures/icons/reload.png");
    private static final ResourceLocation ICON_CLOSE = new ResourceLocation("worthclient", "textures/icons/close.png");
    private GuiIconButton discordButton;
    private GuiIconButton themeButton;
    private GuiIconButton reloadButton;
    private GuiIconButton closeButton;
    private boolean showThemeSelector = false;
    private float themeSelectorAlpha = 0.0f;
    private static Theme prevTheme = Theme.DARK;
    private static float themeTransitionProgress = 1.0f;
    private long lastThemeUpdate = System.currentTimeMillis();
    private String serverMotd = "Carregando informações...";
    private final String SERVER_STATUS_API = "https://api.mcstatus.io/v2/status/java/redeworth.com";
    private final List<MenuParticle> particles = new CopyOnWriteArrayList<>();

    public enum Theme {
        DARK("Dark", new Color(15, 15, 15, 180), new Color(255, 255, 255), new Color(255, 170, 0)),
        STANDARD("Worth", new Color(30, 20, 10, 160), new Color(255, 220, 50), new Color(255, 215, 0)),
        LIGHT("Light", new Color(240, 240, 240, 180), new Color(40, 40, 40), new Color(255, 140, 0)),
        DRACULA("Dracula", new Color(40, 42, 54, 180), new Color(248, 248, 242), new Color(189, 147, 249)),
        OCEAN("Ocean", new Color(10, 25, 47, 180), new Color(200, 230, 255), new Color(100, 255, 218)),
        MAGMA("Magma", new Color(20, 5, 5, 180), new Color(255, 200, 200), new Color(255, 69, 0));

        String name;
        Color overlayColor;
        Color textColor;
        public Color accentColor;

        Theme(String name, Color overlay, Color text, Color accent) {
            this.name = name;
            this.overlayColor = overlay;
            this.textColor = text;
            this.accentColor = accent;
        }
    }

    public static Theme currentTheme = Theme.DARK;

    public GuiClientMainMenu() {
        loadTheme();
        prevTheme = currentTheme;
        themeTransitionProgress = 1.0f;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        this.particles.clear();
        spawnParticles(40);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int btnWidth = 180;
        int btnHeight = 26;
        int spacing = 32;
        int startY = centerY - 25;

        this.closeButton = new GuiIconButton(3, this.width - 35, 10, 24, 24, ICON_CLOSE);
        this.buttonList.add(this.closeButton);

        this.buttonList.add(new GuiModernButton(0, centerX - (btnWidth / 2), startY, btnWidth, btnHeight, "Seus Mundos", 500L));
        this.buttonList.add(new GuiModernButton(1, centerX - (btnWidth / 2), startY + spacing, btnWidth, btnHeight, "Servidores", 600L));
        this.buttonList.add(new GuiModernButton(2, centerX - (btnWidth / 2), startY + spacing * 2, btnWidth, btnHeight, "Opções", 700L));

        int iconSize = 24;
        int iconSpacing = 20;
        int bottomY = this.height - 45;
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
    }

    private void updateThemeTransition() {
        if (themeTransitionProgress < 1.0f) {
            long now = System.currentTimeMillis();
            float delta = (now - lastThemeUpdate) / 1000.0f;
            lastThemeUpdate = now;

            themeTransitionProgress += delta * 3.0f;
            if (themeTransitionProgress > 1.0f) themeTransitionProgress = 1.0f;
        } else {
            lastThemeUpdate = System.currentTimeMillis();
        }
    }

    private Color getThemeColor(Function<Theme, Color> colorExtractor) {
        if (themeTransitionProgress >= 1.0f) return colorExtractor.apply(currentTheme);

        Color c1 = colorExtractor.apply(prevTheme);
        Color c2 = colorExtractor.apply(currentTheme);

        float t = AnimationUtil.easeOutCubic(themeTransitionProgress);

        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * t);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * t);

        return new Color(r, g, b, a);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateThemeTransition();

        float uiAlpha = 1.0f;

        drawDefaultBackground();
        updateAndDrawParticles(mouseX, mouseY);
        drawLogo(uiAlpha);
        drawMotd(uiAlpha);

        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiModernButton) {
                ((GuiModernButton) button).drawButton(this.mc, mouseX, mouseY, uiAlpha);
            } else {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.color(1f, 1f, 1f, uiAlpha);
                button.drawButton(this.mc, mouseX, mouseY);
                GlStateManager.popMatrix();
            }
        }

        drawFooter(uiAlpha);

        float targetThemeAlpha = showThemeSelector ? 1.0f : 0.0f;
        themeSelectorAlpha = themeSelectorAlpha + (targetThemeAlpha - themeSelectorAlpha) * 0.2f;
        if (themeSelectorAlpha > 0.01f) {
            drawThemeSelector(mouseX, mouseY, themeSelectorAlpha);
        }

        drawTooltips(mouseX, mouseY, uiAlpha);
    }

    private void drawFooter(float alpha) {
        int color = new Color(150, 150, 150, (int)(255 * alpha)).getRGB();
        int disclamerColor = new Color(100, 100, 100, (int)(180 * alpha)).getRGB();

        this.drawCenteredString(this.fontRendererObj, "WorthClient © 2026 - Developed by vitorxp", this.width / 2, this.height - 15, color);

        String disclaimer = "WorthClient não é afiliado à Mojang AB.";
        int dWidth = this.fontRendererObj.getStringWidth(disclaimer);
        this.drawString(this.fontRendererObj, disclaimer, this.width - dWidth - 5, this.height - 10, disclamerColor);
    }

    private void drawTooltips(int mouseX, int mouseY, float uiAlpha) {
        if (uiAlpha > 0.9f && (!showThemeSelector || themeSelectorAlpha < 0.1f)) {
            List<String> tooltip = null;
            for (GuiButton button : this.buttonList) {
                if (button.isMouseOver()) {
                    if (button.id == 3) tooltip = Collections.singletonList("Sair do Jogo");
                    if (button.id == 4) tooltip = Collections.singletonList("Reiniciar Texturas");
                    if (button.id == 5) tooltip = Collections.singletonList("Entrar no Discord");
                    if (button.id == 7) tooltip = Collections.singletonList("Alterar Tema");
                }
            }

            if (tooltip != null) {
                GlStateManager.pushMatrix();
                this.drawHoveringText(tooltip, mouseX, mouseY);
                GlStateManager.popMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableLighting();
            }
        }
    }

    private void drawThemeSelector(int mouseX, int mouseY, float alpha) {
        GlStateManager.pushMatrix();
        int alphaInt = (int)(100 * alpha);
        drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, alphaInt).getRGB());

        float scale = 0.9f + (0.1f * alpha);
        GlStateManager.translate(this.width / 2.0f, this.height / 2.0f, 0);
        GlStateManager.scale(scale, scale, 1.0f);
        GlStateManager.translate(-(this.width / 2.0f), -(this.height / 2.0f), 0);

        int numThemes = Theme.values().length;
        int itemHeight = 25;
        int headerHeight = 35;
        int padding = 10;
        int w = 200;
        int h = headerHeight + (numThemes * (itemHeight + 5)) + padding;

        int x = (this.width / 2) - (w / 2);
        int y = (this.height / 2) - (h / 2);

        Color menuBgColor = getThemeColor(t -> t == Theme.LIGHT ? new Color(0xF5F5F5) : new Color(0x121212));
        Color textColor = getThemeColor(t -> t == Theme.LIGHT ? new Color(0x222222) : new Color(0xEEEEEE));
        Color accentColor = getThemeColor(t -> t.accentColor);

        drawRoundedRect(x, y, w, h, 8, menuBgColor.getRGB());
        drawRoundedOutline(x, y, w, h, 8, 1.5f, accentColor.getRGB());

        this.fontRendererObj.drawString("Selecione o Tema", x + 15, y + 12, textColor.getRGB());

        drawRect(x + 10, y + 28, x + w - 10, y + 29, new Color(100, 100, 100, 50).getRGB());

        int btnY = y + headerHeight;
        for (Theme theme : Theme.values()) {
            boolean hover = mouseX >= x + 10 && mouseX <= x + w - 10 && mouseY >= btnY && mouseY <= btnY + itemHeight;
            boolean isSelected = currentTheme == theme;

            int itemBg;
            if (hover) {
                itemBg = new Color(theme.accentColor.getRed(), theme.accentColor.getGreen(), theme.accentColor.getBlue(), 80).getRGB();
            } else if (isSelected) {
                itemBg = new Color(theme.accentColor.getRed(), theme.accentColor.getGreen(), theme.accentColor.getBlue(), 40).getRGB();
            } else {
                itemBg = currentTheme == Theme.LIGHT ? 0xFFEAEAEA : 0xFF1E1E1E;
            }

            drawRoundedRect(x + 10, btnY, w - 20, itemHeight, 4, itemBg);

            int nameColor = (hover || isSelected) ? theme.accentColor.getRGB() : (currentTheme == Theme.LIGHT ? 0xFF555555 : 0xFFAAAAAA);
            this.fontRendererObj.drawString(theme.name, x + 20, btnY + 8, nameColor);

            if (isSelected) {
                this.fontRendererObj.drawString("✔", x + w - 30, btnY + 8, theme.accentColor.getRGB());
            }

            btnY += itemHeight + 5;
        }
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        if (showThemeSelector && themeSelectorAlpha > 0.8f) {
            int numThemes = Theme.values().length;
            int itemHeight = 25;
            int headerHeight = 35;
            int padding = 10;
            int w = 200;
            int h = headerHeight + (numThemes * (itemHeight + 5)) + padding;
            int x = (this.width / 2) - (w / 2);
            int y = (this.height / 2) - (h / 2);

            int btnY = y + headerHeight;

            for (Theme theme : Theme.values()) {
                if (mouseX >= x + 10 && mouseX <= x + w - 10 && mouseY >= btnY && mouseY <= btnY + itemHeight) {
                    if (currentTheme != theme) {
                        prevTheme = currentTheme;
                        currentTheme = theme;
                        themeTransitionProgress = 0.0f;
                        lastThemeUpdate = System.currentTimeMillis();
                        saveTheme();
                    }

                    this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    showThemeSelector = false;
                    return;
                }
                btnY += itemHeight + 5;
            }

            if (mouseX < x || mouseX > x + w || mouseY < y || mouseY > y + h) {
                showThemeSelector = false;
            }
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (showThemeSelector) return;

        switch (button.id) {
            case 0: this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiSelectWorld(this)); break;
            case 1: this.mc.displayGuiScreen(new GuiMultiplayerCustom(this)); break;
            case 2: this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiOptions(this, this.mc.gameSettings)); break;
            case 3: this.mc.shutdown(); break;
            case 4: this.mc.refreshResources(); break;
            case 5: openDiscord("https://discord.gg/VWHvq9zpeV"); break;
            case 7: showThemeSelector = true; break;
        }
    }

    private void saveTheme() {
        try {
            File configFile = new File(Minecraft.getMinecraft().mcDataDir, "worthclient_theme.properties");
            Properties props = new Properties();
            props.setProperty("theme", currentTheme.name());
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "WorthClient Configuration");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTheme() {
        try {
            File configFile = new File(Minecraft.getMinecraft().mcDataDir, "worthclient_theme.properties");
            if (configFile.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                    String themeName = props.getProperty("theme", "DARK");
                    try {
                        currentTheme = Theme.valueOf(themeName);
                    } catch (IllegalArgumentException e) {
                        currentTheme = Theme.DARK;
                    }
                }
            }
        } catch (Exception e) {
            currentTheme = Theme.DARK;
        }
    }

    public void drawDefaultBackground() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);

        Color overlay = getThemeColor(t -> t.overlayColor);
        drawRect(0, 0, this.width, this.height, overlay.getRGB());
    }

    private void drawLogo(float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

        float yOffset = (float) Math.sin(System.currentTimeMillis() / 1200.0F) * 6.0F;

        int logoWidth = 235;
        int logoHeight = 48;
        int logoX = this.width / 2 - logoWidth / 2;
        int logoY = (this.height / 3) - logoHeight - 15;

        this.mc.getTextureManager().bindTexture(MAIN_LOGO);
        drawModalRectWithCustomSizedTexture(logoX, (int)(logoY + yOffset), 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);
        GlStateManager.popMatrix();
    }

    private void drawMotd(float alpha) {
        if (serverMotd != null && !serverMotd.isEmpty()) {
            int logoHeight = 48;
            int logoY = (this.height / 3) - logoHeight - 15;
            int motdY = logoY + logoHeight + 15;

            String[] lines = serverMotd.split("\n");

            Color textColor = getThemeColor(t -> t.textColor);
            int color = textColor.getRGB();
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

    private void spawnParticles(int count) {
        if (this.width <= 0 || this.height <= 0) return;

        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            particles.add(new MenuParticle(
                    rand.nextInt(this.width),
                    rand.nextInt(this.height),
                    (rand.nextFloat() - 0.5f) * 0.5f,
                    (rand.nextFloat() - 0.5f) * 0.5f,
                    rand.nextFloat() * 2.0f + 1.0f
            ));
        }
    }

    private void updateAndDrawParticles(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Color pColor = getThemeColor(t -> t.accentColor);

        for (MenuParticle p : particles) {
            p.update(this.width, this.height);

            float alpha = 0.3f;

            double dist = Math.sqrt(Math.pow(p.x - mouseX, 2) + Math.pow(p.y - mouseY, 2));
            if (dist < 100) {
                alpha = 0.6f;
            }

            float r = pColor.getRed() / 255f;
            float g = pColor.getGreen() / 255f;
            float b = pColor.getBlue() / 255f;

            GlStateManager.color(r, g, b, alpha);
            drawRoundedRect((float)p.x, (float)p.y, p.size, p.size, p.size/2, new Color(r,g,b,alpha).getRGB());
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static class MenuParticle {
        float x, y, velX, velY, size;

        public MenuParticle(float x, float y, float velX, float velY, float size) {
            this.x = x;
            this.y = y;
            this.velX = velX;
            this.velY = velY;
            this.size = size;
        }

        public void update(int w, int h) {
            x += velX;
            y += velY;

            if (x < -10) x = w + 10;
            if (x > w + 10) x = -10;
            if (y < -10) y = h + 10;
            if (y > h + 10) y = -10;
        }
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
        for (int i = 0; i <= 90; i += 5)
            worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 90; i <= 180; i += 5)
            worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 180; i <= 270; i += 5)
            worldrenderer.pos(x + radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 270; i <= 360; i += 5)
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
        for (int i = 0; i <= 90; i += 5)
            worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 90; i <= 180; i += 5)
            worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 180; i <= 270; i += 5)
            worldrenderer.pos(x + radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0.0D).endVertex();
        for (int i = 270; i <= 360; i += 5)
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