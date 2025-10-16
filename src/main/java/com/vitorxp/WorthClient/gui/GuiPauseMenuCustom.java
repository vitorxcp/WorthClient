package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import com.vitorxp.WorthClient.gui.utils.AnimationUtil;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class GuiPauseMenuCustom extends GuiScreen {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("worthclient", "textures/gui/Background_3.png");

    private long animationStartTime;
    private boolean isOpening, isClosing;
    private final int ANIMATION_DURATION_MS = 1000;
    private GuiScreen nextScreen = null;
    private boolean shouldResumeGame = false;

    @Override
    public void initGui() {
        this.buttonList.clear();
        int buttonY = this.height / 2 - 50;
        int buttonSpacing = 30;
        this.buttonList.add(new GuiModernButton(0, this.width / 2 - 100, buttonY, "Voltar ao Jogo", 500L));
        this.buttonList.add(new GuiModernButton(1, this.width / 2 - 100, buttonY + buttonSpacing, "Servidores", 600L));
        this.buttonList.add(new GuiModernButton(2, this.width / 2 - 100, buttonY + buttonSpacing * 2, "Opções", 700L));
        this.buttonList.add(new GuiModernButton(3, this.width / 2 - 100, buttonY + buttonSpacing * 3, "Desconectar", 800L));

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

        drawCustomBackground();

        float uiAlpha = 1.0f;
        if (isOpening) uiAlpha = easedProgress;
        if (isClosing) uiAlpha = 1.0f - easedProgress;
        uiAlpha = Math.max(0.0f, uiAlpha);

        this.drawCenteredString(this.fontRendererObj, "Jogo Pausado", this.width / 2, 40, new Color(1.0f, 1.0f, 1.0f, uiAlpha).getRGB());

        for (net.minecraft.client.gui.GuiButton button : this.buttonList) {
            if (button instanceof GuiModernButton) {
                ((GuiModernButton) button).drawButton(this.mc, mouseX, mouseY, uiAlpha);
            } else {
                button.drawButton(this.mc, mouseX, mouseY);
            }
        }

        if (isOpening || isClosing) {
            float transitionEffectProgress = isOpening ? 1.0f - easedProgress : easedProgress;
            drawSlidingBarsTransition(transitionEffectProgress);

            if (progress >= 1.0f) {
                if (isOpening) isOpening = false;
                if (isClosing) {
                    if (shouldResumeGame) {
                        this.mc.displayGuiScreen(null);
                        this.mc.setIngameFocus();
                    } else {
                        this.mc.displayGuiScreen(this.nextScreen);
                    }
                }
            }
        }
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
        if (isClosing || isOpening) return;

        switch (button.id) {
            case 0: // Voltar ao Jogo
                this.shouldResumeGame = true;
                triggerExitAnimation(null);
                break;
            case 1: // Servidores
                triggerExitAnimation(new GuiMultiplayerCustom(this));
                break;
            case 2: // Opções
                triggerExitAnimation(new GuiOptions(this, this.mc.gameSettings));
                break;
            case 3:
                button.enabled = false;
                this.mc.addScheduledTask(() -> {
                    if (this.mc.theWorld == null) return;

                    NetHandlerPlayClient netHandler = this.mc.getNetHandler();
                    if (netHandler != null) {
                        netHandler.getNetworkManager().closeChannel(new ChatComponentText("Disconnecting"));
                    }
                    this.mc.loadWorld(null);

                    if (netHandler != null) {
                        this.mc.displayGuiScreen(new GuiMultiplayerCustom(new GuiClientMainMenu()));
                    } else {
                        this.mc.displayGuiScreen(new GuiClientMainMenu());
                    }
                });
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            this.shouldResumeGame = true;
            triggerExitAnimation(null);
        }
    }

    public void drawCustomBackground() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);
        drawRect(0, 0, this.width, this.height, new Color(10, 10, 15, 100).getRGB());
    }

    // --- Métodos de Animação (sem alterações) ---
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
        for (int i = 0; i < numBars; i++) {
            float startY = barHeight * i;
            float endY = barHeight * (i + 1);
            float offset = ((w + 100) * progress);
            float x1_0 = (i % 2 == 0) ? -(w + 100) + offset : (w + 100) - offset;
            float x2_0 = x1_0 + (w + 100);
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
}