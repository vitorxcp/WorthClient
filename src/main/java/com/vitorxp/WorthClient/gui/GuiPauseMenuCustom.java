package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import com.vitorxp.WorthClient.gui.utils.AnimationUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import java.awt.Color;
import java.io.IOException;

public class GuiPauseMenuCustom extends GuiScreen {

    private static final ResourceLocation CLIENT_LOGO_LOC = new ResourceLocation("worthclient", "textures/gui/logo_client.png");

    private long initTime;
    private boolean isClosing = false;

    @Override
    public void initGui() {
        this.initTime = System.currentTimeMillis();

        if (!mc.entityRenderer.isShaderActive()) {
            try {
                mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
            } catch (Exception ignored) {}
        }

        this.buttonList.clear();

        int buttonWidth = 200;
        int buttonHeight = 25;
        int spacing = 28;
        int totalMenuHeight = (4 * spacing) + buttonHeight;
        int buttonX = (this.width - buttonWidth) / 2;
        int startY = (this.height - totalMenuHeight) / 2;

        if (startY < 60) startY = 60;

        this.buttonList.add(new GuiModernButton(0, buttonX, startY, "Voltar ao Jogo", 0));
        this.buttonList.add(new GuiModernButton(1, buttonX, startY + spacing, "Servidores", 100));
        this.buttonList.add(new GuiModernButton(2, buttonX, startY + spacing * 2, "Opções", 200));
        this.buttonList.add(new GuiModernButton(4, buttonX, startY + spacing * 3, "Texturas", 300));
        this.buttonList.add(new GuiModernButton(3, buttonX, startY + spacing * 4, "Desconectar", 400));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float animProgress = Math.min(1.0f, (System.currentTimeMillis() - initTime) / 300.0f);
        animProgress = AnimationUtil.easeOutCubic(animProgress);

        this.drawGradientRect(0, 0, this.width, this.height, new Color(0, 0, 0, 100).getRGB(), new Color(0, 0, 0, 200).getRGB());

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2.0f, 40, 0);
        float scale = 1.0f + (0.1f * (1 - animProgress));
        GlStateManager.scale(scale, scale, 1.0f);
        this.drawCenteredString(this.fontRendererObj, "JOGO PAUSADO", 0, 0, -1);
        GlStateManager.popMatrix();

        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiModernButton) {
                ((GuiModernButton) button).drawButton(this.mc, mouseX, mouseY, animProgress);
            } else {
                button.drawButton(this.mc, mouseX, mouseY);
            }
        }

        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        try {
            this.mc.getTextureManager().bindTexture(CLIENT_LOGO_LOC);

            int logoH = 25;
            int logoW = (int) (logoH * (1640.0f / 664.0f));

            int logoX = 5;
            int logoY = this.height - logoH - 5;

            drawModalRectWithCustomSizedTexture(logoX, logoY, 0, 0, logoW, logoH, logoW, logoH);
        } catch (Exception ignored) {
            this.drawString(fontRendererObj, "WorthClient", 5, this.height - 12, 0x808080);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                this.mc.displayGuiScreen(null);
                this.mc.setIngameFocus();
                break;
            case 1:
                this.mc.displayGuiScreen(new GuiMultiplayerCustom(new GuiClientMainMenu()));
                break;
            case 2:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case 3:
                button.enabled = false;
                this.mc.theWorld.sendQuittingDisconnectingPacket();
                this.mc.loadWorld(null);
                System.out.println("Desconectado do Servidor.");
                this.mc.displayGuiScreen(new GuiClientMainMenu());
                break;
            case 4:
                this.mc.displayGuiScreen(new GuiScreenWorthPacks(this));
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
        }
    }

    @Override
    public void onGuiClosed() {
        if (mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.stopUseShader();
        }
    }
}