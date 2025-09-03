package com.vitorxp.SkyBlockModVX.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiMainMenuCustom extends GuiMainMenu {

    private List<GuiButton> customButtons = new ArrayList<>();
    private ResourceLocation[] panoramaTextures = new ResourceLocation[6];
    private float rotation = 0f;
    private float buttonAlpha = 0f;

    @Override
    public void initGui() {
        buttonList.clear();
        customButtons.clear();

        // Inicializa botões
        customButtons.add(new GuiButton(0, width / 2 - 100, height / 2 - 30, 200, 20, "Singleplayer"));
        customButtons.add(new GuiButton(1, width / 2 - 100, height / 2, 200, 20, "Multiplayer"));
        customButtons.add(new GuiButton(2, width / 2 - 100, height / 2 + 30, 200, 20, "Options"));
        customButtons.add(new GuiButton(3, width / 2 - 100, height / 2 + 60, 200, 20, "Quit"));

        buttonList.addAll(customButtons);

        // Carrega panoramas
        for (int i = 0; i < 6; i++) {
            panoramaTextures[i] = new ResourceLocation("textures/gui/title/background/panorama_" + i + ".png");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawPanorama(partialTicks);
        drawGradientRect(0, 0, width, height, 0x88000000, 0x88000000); // overlay

        // Fade-in nos botões
        if (buttonAlpha < 1f) buttonAlpha += 0.02f;

        for (GuiButton button : buttonList) {
            int color = isMouseOverButton(button, mouseX, mouseY)
                    ? fadeColor(0xFFFFFF, 0xAAAAAA, buttonAlpha)
                    : fadeColor(0xDDDDDD, 0x888888, buttonAlpha);

            drawRect(button.xPosition, button.yPosition,
                    button.xPosition + button.width,
                    button.yPosition + button.height,
                    color);

            drawCenteredString(fontRendererObj, button.displayString,
                    button.xPosition + button.width / 2,
                    button.yPosition + 6, 0x000000);
        }

        // Título animado
        drawCenteredString(fontRendererObj, "SkyBlockModVX", width / 2, 20, 0xFFFFFF);
    }

    private boolean isMouseOverButton(GuiButton button, int mouseX, int mouseY) {
        return mouseX >= button.xPosition && mouseY >= button.yPosition
                && mouseX < button.xPosition + button.width
                && mouseY < button.yPosition + button.height;
    }

    private int fadeColor(int startColor, int endColor, float alpha) {
        int sr = (startColor >> 16) & 0xFF;
        int sg = (startColor >> 8) & 0xFF;
        int sb = startColor & 0xFF;

        int er = (endColor >> 16) & 0xFF;
        int eg = (endColor >> 8) & 0xFF;
        int eb = endColor & 0xFF;

        int r = (int) (sr + (er - sr) * alpha);
        int g = (int) (sg + (eg - sg) * alpha);
        int b = (int) (sb + (eb - sb) * alpha);

        return 0xFF << 24 | r << 16 | g << 8 | b;
    }

    private void drawPanorama(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, -50f);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        // Rotação do cubo panorâmico
        GlStateManager.rotate(rotation, 0, 1, 0);
        rotation += 0.2f;

        float size = 120f;

        // Desenha as 6 faces do cubo
        for (int i = 0; i < 6; i++) {
            mc.getTextureManager().bindTexture(panoramaTextures[i]);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor4f(1f, 1f, 1f, 1f);

            switch (i) {
                case 0: // Frente
                    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(-size, -size, size);
                    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(size, -size, size);
                    GL11.glTexCoord2f(1, 1); GL11.glVertex3f(size, size, size);
                    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(-size, size, size);
                    break;
                case 1: // Trás
                    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(size, -size, -size);
                    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(-size, -size, -size);
                    GL11.glTexCoord2f(1, 1); GL11.glVertex3f(-size, size, -size);
                    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(size, size, -size);
                    break;
                case 2: // Cima
                    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(-size, size, size);
                    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(size, size, size);
                    GL11.glTexCoord2f(1, 1); GL11.glVertex3f(size, size, -size);
                    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(-size, size, -size);
                    break;
                case 3: // Baixo
                    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(-size, -size, -size);
                    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(size, -size, -size);
                    GL11.glTexCoord2f(1, 1); GL11.glVertex3f(size, -size, size);
                    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(-size, -size, size);
                    break;
                case 4: // Esquerda
                    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(-size, -size, -size);
                    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(-size, -size, size);
                    GL11.glTexCoord2f(1, 1); GL11.glVertex3f(-size, size, size);
                    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(-size, size, -size);
                    break;
                case 5: // Direita
                    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(size, -size, size);
                    GL11.glTexCoord2f(1, 0); GL11.glVertex3f(size, -size, -size);
                    GL11.glTexCoord2f(1, 1); GL11.glVertex3f(size, size, -size);
                    GL11.glTexCoord2f(0, 1); GL11.glVertex3f(size, size, size);
                    break;
            }

            GL11.glEnd();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        Minecraft mc = Minecraft.getMinecraft();

        switch (button.id) {
            case 0:
                mc.displayGuiScreen(null);
                break;
            case 1:
                mc.displayGuiScreen(new net.minecraft.client.gui.GuiMultiplayer(this));
                break;
            case 2:
                mc.displayGuiScreen(new net.minecraft.client.gui.GuiOptions(this, mc.gameSettings));
                break;
            case 3:
                mc.shutdown();
                break;
        }
    }
}
